package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;

/** Turns you into a flamethrower: a cone of fire pours out wherever you look for a few seconds. */
public final class Flamethrower extends Cannon {
    public Flamethrower() {
        super("&6&lFlamethrower", Material.BLAZE_POWDER, "Turns you into a flamethrower: a cone of fire", "pours out wherever you look.");
        warning(0, "#FF8020");
        option("seconds", 6, "How long it fires.");
        option("range", 14.0, "Flame range.");
        option("cone", 0.35, "Cone width (higher = wider).");
        option("damage", 2.0, "Damage every few ticks.");
    }

    @Override
    public void strike(Strike s) {
        Player me = s.shooter;
        if (me == null) return;
        double range = d("range"), cone = d("cone");
        s.every(1, i("seconds") * 20, t -> {
            if (!me.isOnline() || me.isDead()) return;
            Location eye = me.getEyeLocation();
            Vector look = eye.getDirection();
            for (int k = 0; k < 12; k++) {
                Vector v = look.clone().add(new Vector(Math.random() - 0.5, Math.random() - 0.5, Math.random() - 0.5).multiply(cone)).normalize();
                me.getWorld().spawnParticle(Particle.FLAME, eye.clone().add(look), 0, v.getX(), v.getY(), v.getZ(), range / 14.0, null, true);
            }
            if (t % 4 != 0) return;
            for (Entity e : me.getWorld().getNearbyEntities(eye, range, range, range)) {
                if (!(e instanceof LivingEntity le) || s.spared(e)) continue;
                Vector to = le.getLocation().add(0, 1, 0).toVector().subtract(eye.toVector());
                if (to.length() > range || to.normalize().dot(look) < 1 - cone) continue;
                s.damage(le, d("damage"));
                le.setFireTicks(80);
            }
            if (s.breaks() && t % 8 == 0) {
                Block b = eye.clone().add(look.clone().multiply(range * Math.random())).getBlock();
                if (b.getType().isAir() && b.getRelative(0, -1, 0).getType().isSolid()) Blocks.put(b, Material.FIRE);
            }
            if (t % 6 == 0) me.getWorld().playSound(eye, "entity.blaze.shoot", 1f, 0.6f);
        });
    }
}
