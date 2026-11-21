package dev.megaorbitals;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Typed access to config.yml plus the cooldown / rate-limit bookkeeping it drives. */
public final class Settings {

    public static final int CONFIG_VERSION = 2;

    private final MegaOrbitals plugin;
    private final Map<String, Long> cooldowns = new HashMap<>();
    private final Deque<Long> recentFires = new ArrayDeque<>();

    public Settings(MegaOrbitals plugin) {
        this.plugin = plugin;
    }

    private FileConfiguration c() { return plugin.getConfig(); }

    // general
    public double maxRange() { return c().getDouble("general.max-range", 0); }
    public int blocksPerTick() { return c().getInt("general.blocks-per-tick", 40000); }
    public boolean debug() { return c().getBoolean("general.debug", false); }

    // worlds
    public boolean worldAllowed(World w) {
        boolean listed = c().getStringList("worlds.list").stream().anyMatch(n -> n.equalsIgnoreCase(w.getName()));
        return "whitelist".equalsIgnoreCase(c().getString("worlds.mode", "blacklist")) == listed;
    }

    // safety
    public boolean allowBlockDamage() { return c().getBoolean("safety.allow-block-damage", true); }
    public double damageMultiplier() { return c().getDouble("safety.damage-multiplier", 1.0); }
    /** null = use the cannon's own setting. */
    public Boolean hurtShooterOverride() {
        String v = c().getString("safety.hurt-shooter", "default").toLowerCase();
        return v.equals("always") ? Boolean.TRUE : v.equals("never") ? Boolean.FALSE : null;
    }
    public boolean respectImmunity() { return c().getBoolean("safety.respect-immunity", true); }
    public boolean spawnProtected(Location target) {
        int r = c().getInt("safety.spawn-protection-radius", 0);
        if (r <= 0) return false;
        Location spawn = target.getWorld().getSpawnLocation();
        double dx = spawn.getX() - target.getX(), dz = spawn.getZ() - target.getZ();
        return dx * dx + dz * dz <= (double) r * r;
    }

    /** Records a fire for the server-wide rate limit. Returns false if over the limit. */
    public boolean tryRateLimit() {
        int max = c().getInt("safety.max-fires-per-minute", 0);
        long now = System.currentTimeMillis();
        while (!recentFires.isEmpty() && now - recentFires.peekFirst() > 60_000) recentFires.pollFirst();
        if (max > 0 && recentFires.size() >= max) return false;
        recentFires.addLast(now);
        return true;
    }

    // cooldowns
    /** Seconds left on the player's cooldown for this cannon (0 = ready). */
    public long cooldownLeft(Player p, Cannon cannon, boolean fromItem) {
        if (p.hasPermission(c().getString("cooldowns.bypass-permission", "megaorbitals.bypass.cooldown"))) return 0;
        long now = System.currentTimeMillis();
        long left = 0;
        Long g = cooldowns.get(p.getUniqueId() + (fromItem ? ":item" : ":cmd"));
        if (g != null) left = Math.max(left, g - now);
        Long pc = cooldowns.get(p.getUniqueId() + ":" + cannon.id());
        if (pc != null) left = Math.max(left, pc - now);
        return (long) Math.ceil(left / 1000.0);
    }

    public void startCooldown(Player p, Cannon cannon, boolean fromItem) {
        long now = System.currentTimeMillis();
        int global = c().getInt(fromItem ? "cooldowns.item-seconds" : "cooldowns.command-seconds", 0);
        if (global > 0) cooldowns.put(p.getUniqueId() + (fromItem ? ":item" : ":cmd"), now + global * 1000L);
        int per = c().getInt("cooldowns.per-cannon." + cannon.id(), 0);
        if (per > 0) cooldowns.put(p.getUniqueId() + ":" + cannon.id(), now + per * 1000L);
    }

    public void clearCooldowns(UUID player) {
        cooldowns.keySet().removeIf(k -> k.startsWith(player.toString()));
    }

    // items
    public boolean consumeOnUse() { return c().getBoolean("items.consume-on-use", true); }
    public boolean glow() { return c().getBoolean("items.glow", true); }
    public boolean requireSneak() { return c().getBoolean("items.require-sneak", false); }
    public boolean allowOffHand() { return c().getBoolean("items.allow-off-hand", true); }
    public List<String> usageLore() { return c().getStringList("items.usage-lore"); }

    // cannons
    public boolean disabled(Cannon cannon) {
        return c().getStringList("cannons.disabled").stream().anyMatch(n -> n.equalsIgnoreCase(cannon.id()));
    }
    public boolean perCannonPermission() { return c().getBoolean("cannons.per-cannon-permission", false); }

    // announce / logging
    public boolean announce() { return c().getBoolean("announce.enabled", false); }
    public int announceRadius() { return c().getInt("announce.radius", 0); }
    public String announceMessage() { return c().getString("announce.message", ""); }
    public String announceSound() { return c().getString("announce.sound", ""); }
    public boolean logFires() { return c().getBoolean("logging.log-fires", true); }

    // messages
    public String msg(String key) {
        return c().getString("messages." + key, "");
    }
    public String prefixed(String key) {
        return c().getString("messages.prefix", "") + msg(key);
    }
}
