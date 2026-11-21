package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;

/** A massive blast of air launches everyone sideways out of the area. */
public final class Launchpad extends Cannon {
    public Launchpad() {
        super("&a&lLaunchpad", Material.PISTON, "A massive air blast launches everyone", "out of the area, far and fast.");
        warning(20, "#60FF60");
        noBlockDamage();
        option("radius", 20, "Range.");
        option("power", 4.0, "Sideways launch strength.");
        option("lift", 1.6, "Upward launch strength.");
    }

    @Override
    public void strike(Strike s) {
        Location c = s.at();
        for (LivingEntity le : s.victims(c, d("radius"))) {
            Vector out = Fx.dir(c, le.getLocation()).setY(0);
            if (out.lengthSquared() < 0.01) out = new Vector(Math.random() - 0.5, 0, Math.random() - 0.5);
            le.setVelocity(out.normalize().multiply(d("power")).setY(d("lift")));
        }
        Fx.p(s.world, Particle.GUST_EMITTER_LARGE, c, 3, 1, 0);
        Fx.ring(s.world, Particle.CLOUD, c.clone().add(0, 0.5, 0), d("radius") / 2, 60);
        s.sound(c, "entity.wind_charge.wind_burst", 10f, 0.5f);
    }
}
