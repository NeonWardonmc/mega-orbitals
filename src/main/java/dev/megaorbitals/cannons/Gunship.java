package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;

/** A gunship circles overhead, raking anyone below with explosive cannon fire. */
public final class Gunship extends Cannon {
    public Gunship() {
        super("&8&lGunship", Material.IRON_BLOCK, "A gunship circles overhead, raking anyone", "below with explosive cannon fire.");
        warning(30, "#707070");
        option("seconds", 20, "How long it circles.");
        option("orbit-radius", 22.0, "Size of its circle.");
        option("height", 35, "Altitude.");
        option("target-radius", 30.0, "How far it can shoot.");
        option("shots-per-second", 2, "Fire rate.");
        option("power", 2.5, "Explosion power of each shell.");
    }

    @Override
    public void strike(Strike s) {
        BlockDisplay hull = Fx.display(s.at(0, i("height"), 0), Material.GRAY_CONCRETE, 8f, 2f, 4f);
        int ticks = i("seconds") * 20;
        int fireEvery = Math.max(1, 20 / Math.max(1, i("shots-per-second")));
        s.every(1, ticks, t -> {
            double a = 2 * Math.PI * t / 200.0;
            Location ship = s.at(Math.cos(a) * d("orbit-radius"), i("height"), Math.sin(a) * d("orbit-radius"));
            ship.setYaw((float) Math.toDegrees(a));
            if (t % 2 == 0) hull.teleport(ship);
            Fx.p(s.world, Particle.SMOKE, ship, 3, 1, 0.02);
            if (t % 20 == 0) s.sound(ship, "entity.minecart.riding", 8f, 0.5f);
            if (t % fireEvery != 0) return;
            LivingEntity v = s.nearest(s.at(), d("target-radius"));
            Location hit = v != null ? v.getLocation() : s.randomPoint(d("target-radius"));
            Fx.line(s.world, Particle.FLAME, ship, hit, 1.0);
            s.later(4, () -> s.explode(hit, (float) d("power"), false));
            s.sound(ship, "entity.generic.explode", 3f, 2f);
            if (t >= ticks - fireEvery) hull.remove();
        });
        s.later(ticks + 5L, hull::remove);
    }
}
