package dev.megaorbitals;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.RayTraceResult;

import java.io.File;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class MegaOrbitals extends JavaPlugin {

    private static final String CANNON_PACKAGE = "dev/megaorbitals/cannons/";

    /** Marks entities spawned by a cannon (value = cannon id). */
    public NamespacedKey cannonKey;
    /** Falling blocks with this key turn into the stored material when they land. */
    public NamespacedKey landKey;
    /** Falling blocks with this key vanish when they land. */
    public NamespacedKey vanishKey;
    /** Entities with this key ignore their next fall damage. */
    public NamespacedKey noFallKey;
    /** Undo-record id of the strike that spawned an entity. */
    public NamespacedKey strikeKey;
    /** Marks special items (binoculars, cores, satellites); value = item kind. */
    public NamespacedKey itemKey;

    private final Map<String, Cannon> cannons = new TreeMap<>();
    private BlockQueue blockQueue;
    private Settings settings;
    private Rollback rollback;
    private Lang lang;
    private Progression progression;
    private Spotter spotter;
    private Satellites satellites;
    private DiscordAlerts discord;
    private UpdateChecker updates;

    @Override
    public void onEnable() {
        cannonKey = new NamespacedKey(this, "cannon");
        landKey = new NamespacedKey(this, "land_as");
        vanishKey = new NamespacedKey(this, "vanish");
        noFallKey = new NamespacedKey(this, "no_fall");
        strikeKey = new NamespacedKey(this, "strike");
        itemKey = new NamespacedKey(this, "item");
        settings = new Settings(this);
        upgradeConfig();
        lang = new Lang(this);

        blockQueue = new BlockQueue(this);
        blockQueue.runTaskTimer(this, 1L, 1L);
        rollback = new Rollback(this);
        Blocks.rollback = rollback;

        discoverCannons();
        loadCannons();

        progression = new Progression(this);
        spotter = new Spotter(this);
        satellites = new Satellites(this);
        discord = new DiscordAlerts(this);
        updates = new UpdateChecker(this);
        for (org.bukkit.event.Listener l : new org.bukkit.event.Listener[]{progression, spotter, satellites, updates}) {
            getServer().getPluginManager().registerEvents(l, this);
        }
        progression.registerRecipes();
        updates.start();

        OrbitalCommands cmds = new OrbitalCommands(this);
        getCommand("orbital").setExecutor(cmds);
        getCommand("orbital").setTabCompleter(cmds);
        getCommand("orbitalgive").setExecutor(cmds);
        getCommand("orbitalgive").setTabCompleter(cmds);
        getServer().getPluginManager().registerEvents(new OrbitalListener(this), this);
        menu = new OrbitalMenu(this);
        getServer().getPluginManager().registerEvents(menu, this);
    }

    private OrbitalMenu menu;
    public OrbitalMenu menu() { return menu; }
    public Rollback rollback() { return rollback; }
    public Lang lang() { return lang; }
    public Progression progression() { return progression; }
    public Spotter spotter() { return spotter; }
    public Satellites satellites() { return satellites; }

    @Override
    public void onDisable() {
        if (satellites != null) satellites.save();
    }

    /** Old (1.0.x) config.yml files are moved to config-old.yml and replaced with the new layout. */
    private void upgradeConfig() {
        File f = new File(getDataFolder(), "config.yml");
        if (f.exists()) {
            reloadConfig();
            if (getConfig().getInt("config-version", 1) < Settings.CONFIG_VERSION) {
                File old = new File(getDataFolder(), "config-old.yml");
                if (old.exists()) old.delete();
                if (f.renameTo(old)) getLogger().info("Old config.yml moved to config-old.yml; a new one was created.");
            }
        }
        saveDefaultConfig();
        reloadConfig();
        appendNewSections(f);
    }

    /** 1.3.0 added rollback, crafting, spotter, satellites, discord and updates; append them (with comments) to older files. */
    private void appendNewSections(File f) {
        if (getConfig().isSet("rollback") || !f.exists()) return;
        try (var in = getResource("config.yml")) {
            if (in == null) return;
            String bundled = new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            int from = bundled.indexOf("# Messages now live");
            if (from < 0) return;
            String block = "\n# ----------------------------------------------------------------------------\n" + bundled.substring(from);
            java.nio.file.Files.writeString(f.toPath(), block, java.nio.file.StandardOpenOption.APPEND);
            reloadConfig();
            getLogger().info("Added the new 1.3.0 settings (undo, crafting, satellites, discord...) to config.yml.");
        } catch (Exception e) {
            getLogger().warning("Could not add new settings to config.yml: " + e.getMessage());
        }
    }

    /** Finds every cannon class in dev.megaorbitals.cannons inside this jar. */
    private void discoverCannons() {
        try (JarFile jar = new JarFile(getFile())) {
            Enumeration<JarEntry> en = jar.entries();
            while (en.hasMoreElements()) {
                String n = en.nextElement().getName();
                if (!n.startsWith(CANNON_PACKAGE) || !n.endsWith(".class") || n.contains("$")) continue;
                String cls = n.substring(0, n.length() - 6).replace('/', '.');
                try {
                    Class<?> c = Class.forName(cls, true, getClassLoader());
                    if (!Cannon.class.isAssignableFrom(c) || Modifier.isAbstract(c.getModifiers())) continue;
                    Cannon cannon = (Cannon) c.getDeclaredConstructor().newInstance();
                    cannons.put(cannon.id(), cannon);
                    getServer().getPluginManager().registerEvents(cannon, this);
                } catch (Throwable t) {
                    getLogger().warning("Could not load cannon class " + cls + ": " + t);
                }
            }
        } catch (Exception e) {
            getLogger().severe("Could not scan for cannons: " + e);
        }
    }

    /** (Re)loads config.yml and every cannon's configs/<id>.yml. */
    public void loadCannons() {
        reloadConfig();
        File dir = new File(getDataFolder(), "configs");
        dir.mkdirs();
        for (Cannon c : cannons.values()) {
            File f = new File(dir, c.id() + ".yml");
            if (!f.exists() && getResource("configs/" + c.id() + ".yml") != null) saveResource("configs/" + c.id() + ".yml", false);
            try {
                c.load(this, dir);
            } catch (Exception e) {
                getLogger().warning("Could not load config for " + c.id() + ": " + e);
            }
        }
        getLogger().info("Loaded " + cannons.size() + " orbital cannons.");
    }

    public Map<String, Cannon> cannons() { return cannons; }
    public Cannon cannon(String id) { return id == null ? null : cannons.get(id.toLowerCase()); }
    public BlockQueue blockQueue() { return blockQueue; }
    public Settings settings() { return settings; }

    /** Where a player's cannon will land: their spotter mark if they have one, otherwise the block they look at. */
    public Location target(Player p) {
        Location mark = spotter == null ? null : spotter.mark(p);
        return mark != null ? mark : crosshair(p);
    }

    /** Block the player is looking at, or null. */
    public Location crosshair(Player p) {
        double range = settings.maxRange();
        if (range <= 0) range = 2048;
        RayTraceResult r = p.getWorld().rayTraceBlocks(p.getEyeLocation(), p.getEyeLocation().getDirection(), range, FluidCollisionMode.ALWAYS, true);
        if (r == null || r.getHitBlock() == null) return null;
        return r.getHitBlock().getLocation();
    }

    /**
     * Fires a cannon at what the player is looking at, applying every config.yml rule.
     * Returns true if it actually fired (so items should be used up).
     */
    public boolean fire(Cannon c, Player shooter, boolean fromItem) {
        if (settings.disabled(c)) return deny(shooter, "disabled-cannon");
        if (!settings.worldAllowed(shooter.getWorld())) return deny(shooter, "disabled-world");
        if (fromItem && settings.perCannonPermission() && !shooter.hasPermission("megaorbitals.cannon." + c.id())) return deny(shooter, "no-permission");
        long cd = settings.cooldownLeft(shooter, c, fromItem);
        if (cd > 0) {
            shooter.sendActionBar(color(settings.msg("cooldown").replace("{seconds}", String.valueOf(cd))));
            return false;
        }
        Location t = target(shooter);
        if (t == null) return deny(shooter, "no-target");
        if (settings.spawnProtected(t)) return deny(shooter, "spawn-protected");
        Satellites.Sat sat = null;
        if (fromItem && satellites.required(shooter)) {
            sat = satellites.find(shooter, t, progression.cost(c));
            if (sat == null) {
                shooter.sendActionBar(color(settings.msg("no-satellite").replace("{cost}", String.valueOf(progression.cost(c)))));
                return false;
            }
        }
        if (!settings.tryRateLimit()) return deny(shooter, "rate-limited");

        if (sat != null) satellites.spend(sat, progression.cost(c));
        settings.startCooldown(shooter, c, fromItem);
        Strike s = fireAt(c, t, shooter);
        spotter.fired(shooter);
        shooter.sendActionBar(color(settings.msg("fire").replace("{cannon}", c.name())));
        if (settings.logFires()) {
            getLogger().info(shooter.getName() + " fired " + c.id() + " at " + t.getWorld().getName() + " " + t.getBlockX() + " " + t.getBlockY() + " " + t.getBlockZ());
        }
        if (settings.announce()) announce(c, t, shooter);
        discord.alert(c, t, shooter);
        scheduleAutoRestore(c, s);
        return true;
    }

    /** Fires a cannon at a block with no checks at all (used by cannons that fire other cannons). */
    public Strike fireAt(Cannon c, Location targetBlock, Player shooter) {
        return fireAt(c, targetBlock, shooter, null);
    }

    /** Same, but the new strike shares parent's undo record (so one /orbital undo reverts them all). */
    public Strike fireAt(Cannon c, Location targetBlock, Player shooter, Strike parent) {
        Strike s = new Strike(this, c, targetBlock, shooter, parent);
        try {
            s.run(() -> c.warn(s));
        } catch (Exception e) {
            getLogger().warning("Warning effect of " + c.id() + " failed: " + e);
        }
        s.later(c.warningTicks(), () -> {
            if (!intercepted(s)) c.strike(s);
        });
        return s;
    }

    /** True if a cannon such as the Orbital Defence Grid shoots this strike down before it lands. */
    private boolean intercepted(Strike s) {
        for (Cannon other : cannons.values()) {
            if (other == s.cannon || !(other instanceof StrikeInterceptor si)) continue;
            try {
                if (si.intercept(s)) return true;
            } catch (Exception e) {
                getLogger().warning("Interceptor " + other.id() + " failed: " + e);
            }
        }
        return false;
    }

    /** rollback.auto-restore-seconds (or the cannon's own auto-restore-seconds) puts the terrain back automatically. */
    private void scheduleAutoRestore(Cannon c, Strike s) {
        int secs = c.autoRestoreSeconds();
        if (secs < 0) secs = getConfig().getInt("rollback.auto-restore-seconds", 0);
        if (secs <= 0 || s.record() == null) return;
        getServer().getScheduler().runTaskLater(this, () -> rollback.restore(s.record()), c.warningTicks() + secs * 20L);
    }

    private boolean deny(Player p, String msgKey) {
        p.sendActionBar(color(settings.msg(msgKey)));
        return false;
    }

    private void announce(Cannon c, Location t, Player shooter) {
        String text = settings.announceMessage().replace("{player}", shooter.getName()).replace("{cannon}", c.name())
                .replace("{x}", String.valueOf(t.getBlockX())).replace("{y}", String.valueOf(t.getBlockY())).replace("{z}", String.valueOf(t.getBlockZ()));
        int r = settings.announceRadius();
        String sound = settings.announceSound();
        for (Player p : getServer().getOnlinePlayers()) {
            if (r > 0 && (p.getWorld() != t.getWorld() || p.getLocation().distance(t) > r)) continue;
            p.sendMessage(color(text));
            if (sound != null && !sound.isEmpty()) p.playSound(p.getLocation(), sound, 0.6f, 1f);
        }
    }

    public ItemStack createItem(Cannon c, int amount) {
        ItemStack stack = new ItemStack(c.item(), Math.max(1, amount));
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(color(c.name()).decoration(TextDecoration.ITALIC, false));
        List<Component> lore = new ArrayList<>();
        lore.add(color(Catalog.tierColor(c.tier()) + Catalog.pretty(c.tier()) + " &8· &7" + Catalog.pretty(c.category())).decoration(TextDecoration.ITALIC, false));
        for (String l : c.lore()) lore.add(color("&7" + l).decoration(TextDecoration.ITALIC, false));
        String uses = settings.consumeOnUse() ? "One-time use" : "Reusable";
        for (String l : settings.usageLore()) lore.add(color(l.replace("{uses}", uses)).decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);
        if (settings.glow()) meta.setEnchantmentGlintOverride(true);
        meta.getPersistentDataContainer().set(cannonKey, PersistentDataType.STRING, c.id());
        stack.setItemMeta(meta);
        return stack;
    }

    public static Component color(String s) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(s == null ? "" : s);
    }
}
