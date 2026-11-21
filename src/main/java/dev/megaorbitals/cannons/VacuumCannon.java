package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;

/** A vacuum beam: everything in front of you is sucked straight toward you. */
public final class VacuumCannon extends Cannon {
    public VacuumCannon() {
        super("&8&lVacuum Beam", Material.HOPPER, "A vacuum beam: everything in front of you", "is sucked straight toward you.");
        warning(0, "#606060");
        noBlockDamage();
        option("seconds", 6, "How long it sucks.");
        option("range", 25.0, "Range.");
        option("cone", 0.3, "Cone width.");
        option("strength", 0.9, "Pull strength.");
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
            for (int k = 0; k < 6; k++) {
                Location far = eye.clone().add(look.clone().multiply(range * Math.random()));
                me.getWorld().spawnParticle(Particle.PORTAL, far, 0, -look.getX(), -look.getY(), -look.getZ(), 1.5, null, true);
            }
            for (Entity e : me.getWorld().getNearbyEntities(eye, range, range, range)) {
                if (e.equals(me) || (e instanceof LivingEntity && s.spared(e))) continue;
                Vector to = e.getLocation().toVector().subtract(eye.toVector());
                if (to.length() > range || to.length() < 2 || to.clone().normalize().dot(look) < 1 - cone) continue;
                e.setVelocity(look.clone().multiply(-d("strength")).setY(0.15));
            }
            if (t % 10 == 0) me.getWorld().playSound(eye, "block.portal.ambient", 1f, 2f);
        });
    }
}
