package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;

/** An unseen dragon strafes across the target in the direction you face, raining acid breath. */
public final class DragonStrafe extends Cannon {
    public DragonStrafe() {
        super("&5&lDragon Strafe", Material.DRAGON_HEAD, "An unseen dragon strafes across the target", "the way you face, raining acid breath.");
        warning(20, "#A030E0");
        option("length", 80, "Length of the strafing run.");
        option("spacing", 4.0, "Blocks between fireballs.");
        option("height", 25, "Height of the run.");
    }

    @Override
    public void strike(Strike s) {
        Vector dir = s.shooter != null ? s.shooter.getLocation().getDirection().setY(0) : new Vector(1, 0, 0);
        if (dir.lengthSquared() < 0.01) dir = new Vector(1, 0, 0);
        Vector step = dir.normalize().multiply(d("spacing"));
        Location start = s.at(0, i("height"), 0).subtract(step.clone().multiply(d("length") / d("spacing") / 2));
        s.soundWorld("entity.ender_dragon.growl", 0.8f, 0.8f);
        s.every(2, (int) (d("length") / d("spacing")), t -> {
            Location l = start.clone().add(step.clone().multiply(t));
            s.world.spawn(l, DragonFireball.class, f -> {
                f.setDirection(new Vector(0, -1, 0));
                f.setVelocity(new Vector(0, -1.2, 0));
                if (s.shooter != null) f.setShooter(s.shooter);
                s.tag(f);
            });
            Fx.p(s.world, Particle.PORTAL, l, 30, 1, 0.3);
            if (t % 4 == 0) s.sound(l, "entity.ender_dragon.flap", 8f, 0.8f);
        });
    }
}
