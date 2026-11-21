package dev.megaorbitals;

import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Satellites: players launch them (craft + right-click), each covers an area and holds charge that refills over
 * time. With satellites.enabled, firing a cannon ITEM needs a satellite of yours over the target with enough charge
 * (the cannon's tier decides the cost). Admin commands and the bypass permission skip this.
 */
public final class Satellites implements Listener {

    public static final class Sat {
        public final UUID id, owner;
        public final String world;
        public final int x, z;
        double charge;
        long updated;
        Sat(UUID id, UUID owner, String world, int x, int z, double charge, long updated) {
            this.id = id; this.owner = owner; this.world = world; this.x = x; this.z = z; this.charge = charge; this.updated = updated;
        }
    }

    private final MegaOrbitals plugin;
    private final List<Sat> sats = new ArrayList<>();
    private final File file;

    public Satellites(MegaOrbitals plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "satellites.yml");
        load();
        plugin.getServer().getScheduler().runTaskTimer(plugin, this::save, 6000L, 6000L);
    }

    private boolean enabled() { return plugin.getConfig().getBoolean("satellites.enabled", false); }
    private double max() { return plugin.getConfig().getDouble("satellites.max-charge", 100); }
    private double radius() { return plugin.getConfig().getDouble("satellites.radius", 400); }

    /** Does this player need a satellite to fire cannon items? */
    public boolean required(Player p) {
        return enabled() && !p.hasPermission("megaorbitals.bypass.satellite");
    }

    private double charge(Sat s) {
        long now = System.currentTimeMillis();
        double perMin = plugin.getConfig().getDouble("satellites.recharge-per-minute", 5);
        s.charge = Math.min(max(), s.charge + (now - s.updated) / 60000.0 * perMin);
        s.updated = now;
        return s.charge;
    }

    /** The best satellite this player may use over target with at least cost charge, or null. */
    public Sat find(Player p, Location target, int cost) {
        boolean shared = plugin.getConfig().getBoolean("satellites.shared", false);
        Sat best = null;
        for (Sat s : sats) {
            if (!s.world.equals(target.getWorld().getName())) continue;
            if (!shared && !s.owner.equals(p.getUniqueId())) continue;
            double dx = s.x - target.getX(), dz = s.z - target.getZ();
            if (dx * dx + dz * dz > radius() * radius()) continue;
            if (charge(s) >= cost && (best == null || s.charge > best.charge)) best = s;
        }
        return best;
    }

    public void spend(Sat s, int cost) {
        charge(s);
        s.charge = Math.max(0, s.charge - cost);
    }

    public ItemStack satelliteItem() {
        ItemStack it = new ItemStack(Material.DAYLIGHT_DETECTOR);
        ItemMeta m = it.getItemMeta();
        m.displayName(MegaOrbitals.color("&b&lOrbital Satellite").decoration(TextDecoration.ITALIC, false));
        m.lore(List.of(
                MegaOrbitals.color("&eRight-click &7to launch it into orbit").decoration(TextDecoration.ITALIC, false),
                MegaOrbitals.color("&7above where you're standing.").decoration(TextDecoration.ITALIC, false),
                MegaOrbitals.color("&7Powers cannons fired in its area.").decoration(TextDecoration.ITALIC, false)));
        m.setEnchantmentGlintOverride(true);
        m.getPersistentDataContainer().set(plugin.itemKey, PersistentDataType.STRING, "satellite");
        it.setItemMeta(m);
        return it;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onLaunch(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        ItemStack it = e.getItem();
        if (it == null || !it.hasItemMeta() || !"satellite".equals(it.getItemMeta().getPersistentDataContainer().get(plugin.itemKey, PersistentDataType.STRING))) return;
        e.setUseItemInHand(Event.Result.DENY);
        e.setUseInteractedBlock(Event.Result.DENY);
        e.setCancelled(true);
        Player p = e.getPlayer();
        int maxOwned = plugin.getConfig().getInt("satellites.max-per-player", 3);
        if (mine(p).size() >= maxOwned) {
            p.sendActionBar(MegaOrbitals.color(plugin.settings().msg("satellite-limit").replace("{max}", String.valueOf(maxOwned))));
            return;
        }
        it.setAmount(it.getAmount() - 1);
        p.getInventory().setItem(e.getHand() == null ? EquipmentSlot.HAND : e.getHand(), it.getAmount() <= 0 ? null : it);
        Location l = p.getLocation();
        sats.add(new Sat(UUID.randomUUID(), p.getUniqueId(), l.getWorld().getName(), l.getBlockX(), l.getBlockZ(),
                plugin.getConfig().getDouble("satellites.start-charge", 50), System.currentTimeMillis()));
        save();
        Particle.DustOptions blue = new Particle.DustOptions(Color.fromRGB(120, 200, 255), 2f);
        new org.bukkit.scheduler.BukkitRunnable() {
            int t = 0;
            @Override public void run() {
                Location rocket = l.clone().add(0, t * 3, 0);
                l.getWorld().spawnParticle(Particle.FIREWORK, rocket, 10, 0.2, 0.2, 0.2, 0.05, null, true);
                l.getWorld().spawnParticle(Particle.DUST, rocket, 5, 0.2, 0.5, 0.2, 0, blue, true);
                if (++t > 40) cancel();
            }
        }.runTaskTimer(plugin, 0L, 1L);
        p.playSound(l, "entity.firework_rocket.launch", 2f, 0.5f);
        p.sendMessage(MegaOrbitals.color(plugin.settings().prefixed("satellite-launched").replace("{radius}", String.valueOf((int) radius()))));
    }

    public List<Sat> mine(Player p) {
        List<Sat> out = new ArrayList<>();
        for (Sat s : sats) if (s.owner.equals(p.getUniqueId())) out.add(s);
        return out;
    }

    /** /orbital satellites */
    public void list(Player p) {
        List<Sat> own = mine(p);
        if (own.isEmpty()) { p.sendMessage(MegaOrbitals.color(plugin.settings().prefixed("satellite-none"))); return; }
        p.sendMessage(MegaOrbitals.color(plugin.settings().prefixed("satellite-header")));
        int i = 1;
        for (Sat s : own) {
            String dist = p.getWorld().getName().equals(s.world)
                    ? String.valueOf((int) Math.hypot(p.getLocation().getX() - s.x, p.getLocation().getZ() - s.z)) : "?";
            p.sendMessage(MegaOrbitals.color(plugin.settings().msg("satellite-line").replace("{index}", String.valueOf(i++))
                    .replace("{world}", s.world).replace("{x}", String.valueOf(s.x)).replace("{z}", String.valueOf(s.z))
                    .replace("{charge}", String.valueOf((int) charge(s))).replace("{max}", String.valueOf((int) max()))
                    .replace("{distance}", dist)));
        }
    }

    public void save() {
        YamlConfiguration y = new YamlConfiguration();
        for (Sat s : sats) {
            String k = "satellites." + s.id;
            y.set(k + ".owner", s.owner.toString());
            y.set(k + ".world", s.world);
            y.set(k + ".x", s.x);
            y.set(k + ".z", s.z);
            y.set(k + ".charge", charge(s));
        }
        try { y.save(file); } catch (Exception e) { plugin.getLogger().warning("Could not save satellites.yml: " + e.getMessage()); }
    }

    private void load() {
        if (!file.exists()) return;
        YamlConfiguration y = YamlConfiguration.loadConfiguration(file);
        var sec = y.getConfigurationSection("satellites");
        if (sec == null) return;
        long now = System.currentTimeMillis();
        for (String id : sec.getKeys(false)) {
            try {
                sats.add(new Sat(UUID.fromString(id), UUID.fromString(sec.getString(id + ".owner")), sec.getString(id + ".world"),
                        sec.getInt(id + ".x"), sec.getInt(id + ".z"), sec.getDouble(id + ".charge"), now));
            } catch (Exception ex) {
                plugin.getLogger().warning("Skipping bad satellite " + id);
            }
        }
    }
}
