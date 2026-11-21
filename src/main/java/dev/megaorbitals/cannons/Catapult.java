package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;

/** Everyone near the target is gathered up, then catapulted far away in the direction you're facing. */
public final class Catapult extends Cannon {
    public Catapult() {
        super("&6&lCatapult", Material.OAK_PRESSURE_PLATE, "Everyone near the target is gathered up, then", "catapulted far away in the direction you face.");
        warning(15, "#C09050");
        noBlockDamage();
        option("radius", 15, "Range.");
        option("power", 3.0, "Launch strength.");
        option("lift", 1.6, "Launch height.");
    }

    @Override
    public void strike(Strike s) {
        Vector dir = s.shooter != null ? s.shooter.getLocation().getDirection().setY(0) : new Vector(1, 0, 0);
        if (dir.lengthSquared() < 0.01) dir = new Vector(1, 0, 0);
        Vector fling = dir.normalize().multiply(d("power")).setY(d("lift"));
        var caught = s.victims(s.at(), d("radius"));
        s.every(1, 15, t -> {
            for (LivingEntity le : caught) if (le.isValid()) le.setVelocity(Fx.dir(le.getLocation(), s.at()).multiply(0.5).setY(0.1));
        });
        s.later(16, () -> {
            for (LivingEntity le : caught) if (le.isValid()) { le.setVelocity(fling); Fx.p(s.world, Particle.CLOUD, le.getLocation(), 10, 0.3, 0.1); }
            s.sound(s.at(), "entity.breeze.wind_burst", 10f, 0.5f);
        });
    }
}
