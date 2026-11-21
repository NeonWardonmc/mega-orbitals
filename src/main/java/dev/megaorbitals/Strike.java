package dev.megaorbitals;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.util.Vector;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.IntConsumer;
import java.util.function.IntPredicate;

/** One firing of a cannon: where it hits, who fired it, plus helpers cannons use. */
public final class Strike {

    public final MegaOrbitals plugin;
    public final Cannon cannon;
    public final World world;
    public final Player shooter;
    private final Location target;

    public Strike(MegaOrbitals plugin, Cannon cannon, Location targetBlock, Player shooter) {
        this.plugin = plugin;
        this.cannon = cannon;
        this.world = targetBlock.getWorld();
        this.shooter = shooter;
        this.target = targetBlock.clone().add(0.5, 1.0, 0.5);
    }

    /** The impact point: top centre of the targeted block. Always a fresh copy. */
    public Location at() { return target.clone(); }

    /** The targeted block itself (for firing other cannons at the same spot). */
    public Location block() { return target.clone().subtract(0.5, 1.0, 0.5); }

    /** Same as {@link #at()} offset by x/y/z. */
    public Location at(double x, double y, double z) { return target.clone().add(x, y, z); }

    // ------------------------------------------------------------ settings shortcuts
    public int i(String k) { return cannon.i(k); }
    public double d(String k) { return cannon.d(k); }
    public boolean b(String k) { return cannon.b(k); }
    public String str(String k) { return cannon.str(k); }
    public Material mat(String k) { return cannon.mat(k); }
    public boolean breaks() { return cannon.breaksBlocks(); }

    // ------------------------------------------------------------ scheduling
    public BukkitTask later(long ticks, Runnable r) {
        return plugin.getServer().getScheduler().runTaskLater(plugin, guard(r), Math.max(0, ticks));
    }

    /** Runs fn(tick) every period ticks, times times (times <= 0 = forever, so be careful). */
    public BukkitTask every(long period, int times, IntConsumer fn) {
        return loop(period, t -> {
            if (times > 0 && t >= times) return false;
            fn.accept(t);
            return true;
        });
    }

    /** Runs fn(tick) every period ticks until it returns false. */
    public BukkitTask loop(long period, IntPredicate fn) {
        return new BukkitRunnable() {
            int t = 0;
            @Override public void run() {
                boolean go;
                try { go = fn.test(t++); } catch (Exception e) {
                    plugin.getLogger().warning("Cannon " + cannon.id() + " error: " + e);
                    go = false;
                }
                if (!go) cancel();
            }
        }.runTaskTimer(plugin, 0L, Math.max(1, period));
    }

    private Runnable guard(Runnable r) {
        return () -> {
            try { r.run(); } catch (Exception e) { plugin.getLogger().warning("Cannon " + cannon.id() + " error: " + e); }
        };
    }

    // ------------------------------------------------------------ targets
    /** Should this entity be spared (the shooter, unless hurt-shooter is on)? */
    public boolean spared(Entity e) {
        if (e instanceof Player p && plugin.settings().respectImmunity() && p.hasPermission("megaorbitals.immune")) return true;
        return shooter != null && !cannon.hurtsShooter() && e.getUniqueId().equals(shooter.getUniqueId());
    }

    /** Living entities within r blocks of c (not the shooter unless hurt-shooter). */
    public List<LivingEntity> victims(Location c, double r) {
        List<LivingEntity> out = new ArrayList<>();
        double r2 = r * r;
        for (Entity e : c.getWorld().getNearbyEntities(c, r, r, r)) {
            if (e instanceof LivingEntity le && !le.isDead() && !spared(e) && e.getLocation().distanceSquared(c) <= r2) out.add(le);
        }
        return out;
    }

    /** Players within r blocks of c (not the shooter unless hurt-shooter). */
    public List<Player> players(Location c, double r) {
        List<Player> out = new ArrayList<>();
        for (LivingEntity le : victims(c, r)) if (le instanceof Player p) out.add(p);
        return out;
    }

    public void damage(LivingEntity e, double amount) {
        if (spared(e)) return;
        amount *= plugin.settings().damageMultiplier();
        if (shooter != null && !e.equals(shooter)) e.damage(amount, shooter);
        else e.damage(amount);
    }

    // ------------------------------------------------------------ world helpers
    public void explode(Location l, float power, boolean fire) {
        world.createExplosion(l, power, fire, breaks(), shooter);
    }

    /** Queue a block edit job (skipped when break-blocks is false). */
    public void blocks(BlockJob job) {
        if (breaks()) plugin.blockQueue().submit(job);
    }

    /** Ground level at the x/z of l (searching 60 blocks up/down from l). */
    public Location surface(Location l) {
        return Blocks.surface(l);
    }

    /** Random point within r blocks of the target (on the ground). */
    public Location randomPoint(double r) {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        double a = rnd.nextDouble(Math.PI * 2), d = Math.sqrt(rnd.nextDouble()) * r;
        return surface(at(Math.cos(a) * d, 0, Math.sin(a) * d));
    }

    /** Marks an entity as belonging to this cannon (used for block-damage rules). */
    public <T extends Entity> T tag(T e) {
        e.getPersistentDataContainer().set(plugin.cannonKey, PersistentDataType.STRING, cannon.id());
        return e;
    }

    /** Spawns tagged primed TNT that falls straight down (no random sideways kick). */
    public TNTPrimed tnt(Location l, float power, int fuse) {
        return world.spawn(l, TNTPrimed.class, t -> {
            t.setFuseTicks(fuse);
            t.setYield(power);
            t.setVelocity(new Vector(0, 0, 0));
            if (shooter != null) t.setSource(shooter);
            tag(t);
        });
    }

    /** Spawns a tagged falling block. vanish = disappears instead of being placed when it lands. */
    public FallingBlock fallingBlock(Location l, Material m, boolean vanish) {
        return world.spawn(l, FallingBlock.class, f -> {
            f.setBlockData(m.createBlockData());
            f.setDropItem(false);
            f.setCancelDrop(true);
            tag(f);
            if (vanish) f.getPersistentDataContainer().set(plugin.vanishKey, PersistentDataType.BYTE, (byte) 1);
        });
    }

    /** Roughly how many ticks something takes to fall h blocks from rest. */
    public static int fallTicks(double h) {
        double y = 0, v = 0;
        int t = 0;
        while (y < h && t < 600) { v = (v + 0.04) * 0.98; y += v; t++; }
        return t + 1;
    }

    /** Spawns a mob of the given type, tagged and immune to its first fall. */
    public <T extends Entity> T mob(Location l, Class<T> type, java.util.function.Consumer<T> setup) {
        return world.spawn(l, type, e -> {
            tag(e);
            e.getPersistentDataContainer().set(plugin.noFallKey, PersistentDataType.BYTE, (byte) 1);
            if (setup != null) setup.accept(e);
        });
    }

    /** The nearest victim to l within r, or null. */
    public LivingEntity nearest(Location l, double r) {
        LivingEntity best = null;
        double bd = Double.MAX_VALUE;
        for (LivingEntity le : victims(l, r)) {
            double d = le.getLocation().distanceSquared(l);
            if (d < bd) { bd = d; best = le; }
        }
        return best;
    }

    /** The nearest player victim to l within r, or null. */
    public Player nearestPlayer(Location l, double r) {
        Player best = null;
        double bd = Double.MAX_VALUE;
        for (Player p : players(l, r)) {
            double d = p.getLocation().distanceSquared(l);
            if (d < bd) { bd = d; best = p; }
        }
        return best;
    }

    /** Makes a mob hunt the nearest player victim within r. */
    public void hunt(Entity e, double r) {
        if (e instanceof org.bukkit.entity.Mob m) {
            Player p = nearestPlayer(e.getLocation(), r);
            if (p != null) m.setTarget(p);
        }
    }

    /**
     * Temporarily changes an attribute (scale, gravity, max health ...) and undoes it after ticks.
     * amount is added on top of the base value (operation ADD_NUMBER) or as a multiplier (ADD_SCALAR: 0.5 = +50%).
     */
    public void tempAttribute(LivingEntity e, org.bukkit.attribute.Attribute attr, double amount,
                              org.bukkit.attribute.AttributeModifier.Operation op, long ticks) {
        var inst = e.getAttribute(attr);
        if (inst == null) return;
        org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(plugin, "tmp_" + cannon.id() + "_" + java.util.UUID.randomUUID().toString().substring(0, 8));
        inst.addModifier(new org.bukkit.attribute.AttributeModifier(key, amount, op, org.bukkit.inventory.EquipmentSlotGroup.ANY));
        later(ticks, () -> {
            var i2 = e.getAttribute(attr);
            if (i2 != null) i2.removeModifier(key);
        });
    }

    /** Players within r of the shooter, the shooter included (used as "allies" by support cannons). */
    public List<Player> allies(double r) {
        List<Player> out = new ArrayList<>();
        if (shooter == null) return out;
        for (Player p : shooter.getWorld().getPlayers()) if (p.getLocation().distance(shooter.getLocation()) <= r) out.add(p);
        return out;
    }

    /** Removes the entity after the given ticks if it still exists. */
    public <T extends Entity> T expire(T e, long ticks) {
        later(ticks, () -> { if (e.isValid()) e.remove(); });
        return e;
    }

    public void sound(Location l, String key, float volume, float pitch) {
        l.getWorld().playSound(l, key, volume, pitch);
    }

    /** Plays a sound to every player in the world, at their own position. */
    public void soundWorld(String key, float volume, float pitch) {
        for (Player p : world.getPlayers()) p.playSound(p.getLocation(), key, volume, pitch);
    }

    public void actionBar(Player p, String legacy) {
        p.sendActionBar(MegaOrbitals.color(legacy));
    }

    public Component text(String legacy) { return MegaOrbitals.color(legacy); }
}
