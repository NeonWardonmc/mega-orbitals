package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;

/** Straps a rocket to everyone nearby: up they go, trailing sparks, until it blows. */
public final class RocketRide extends Cannon {
    public RocketRide() {
        super("&c&lRocket Ride", Material.FIREWORK_ROCKET, "Straps a rocket to everyone nearby.", "Up they go, until it blows.");
        warning(15, "#FF4040");
        noBlockDamage();
        option("radius", 20, "Range.");
        option("burn-ticks", 35, "How long the rockets fly.");
        option("blast-power", 2.5, "Power of the final blast.");
    }

    @Override
    public void strike(Strike s) {
        for (LivingEntity le : s.victims(s.at(), d("radius"))) {
            s.every(1, i("burn-ticks"), t -> {
                if (!le.isValid()) return;
                le.setVelocity(new Vector(le.getVelocity().getX() * 0.5, 1.3, le.getVelocity().getZ() * 0.5));
                Fx.p(s.world, Particle.FIREWORK, le.getLocation(), 6, 0.2, 0.05);
                Fx.p(s.world, Particle.FLAME, le.getLocation(), 3, 0.1, 0.02);
                if (t % 5 == 0) s.sound(le.getLocation(), "entity.firework_rocket.launch", 2f, 1f);
            });
            s.later(i("burn-ticks"), () -> {
                if (!le.isValid()) return;
                s.world.createExplosion(le.getLocation(), (float) d("blast-power"), false, false, s.shooter);
                Fx.p(s.world, Particle.FIREWORK, le.getLocation(), 60, 1, 0.3);
            });
        }
    }
}
