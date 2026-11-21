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

    private final Map<String, Cannon> cannons = new TreeMap<>();
    private BlockQueue blockQueue;
    private Settings settings;

    @Override
    public void onEnable() {
        cannonKey = new NamespacedKey(this, "cannon");
        landKey = new NamespacedKey(this, "land_as");
        vanishKey = new NamespacedKey(this, "vanish");
        noFallKey = new NamespacedKey(this, "no_fall");
        settings = new Settings(this);
        upgradeConfig();

        blockQueue = new BlockQueue(this);
        blockQueue.runTaskTimer(this, 1L, 1L);

        discoverCannons();
        loadCannons();

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

    /** Block the player is looking at, or null. */
    public Location target(Player p) {
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
        if (!settings.tryRateLimit()) return deny(shooter, "rate-limited");

        settings.startCooldown(shooter, c, fromItem);
        fireAt(c, t, shooter);
        shooter.sendActionBar(color(settings.msg("fire").replace("{cannon}", c.name())));
        if (settings.logFires()) {
            getLogger().info(shooter.getName() + " fired " + c.id() + " at " + t.getWorld().getName() + " " + t.getBlockX() + " " + t.getBlockY() + " " + t.getBlockZ());
        }
        if (settings.announce()) announce(c, t, shooter);
        return true;
    }

    /** Fires a cannon at a block with no checks at all (used by cannons that fire other cannons). */
    public Strike fireAt(Cannon c, Location targetBlock, Player shooter) {
        Strike s = new Strike(this, c, targetBlock, shooter);
        try {
            c.warn(s);
        } catch (Exception e) {
            getLogger().warning("Warning effect of " + c.id() + " failed: " + e);
        }
        s.later(c.warningTicks(), () -> c.strike(s));
        return s;
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
