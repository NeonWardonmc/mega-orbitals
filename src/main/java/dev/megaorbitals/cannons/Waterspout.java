package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;

/** A towering waterspout sucks everyone up into the sky, then lets go. */
public final class Waterspout extends Cannon {
    public Waterspout() {
        super("&9&lWaterspout", Material.WATER_BUCKET, "A towering waterspout sucks everyone", "up into the sky... then lets go.");
        warning(30, "#3080FF");
        noBlockDamage();
        option("radius", 7.0, "Width of the spout.");
        option("height", 45, "How high it carries you.");
        option("seconds", 8, "How long the spout lasts.");
    }

    @Override
    public void strike(Strike s) {
        Location c = s.at();
        int h = i("height");
        double r = d("radius");
        s.every(1, i("seconds") * 20, t -> {
            for (int y = 0; y < h; y += 2) {
                double a = t * 0.4 + y * 0.3;
                Location l = c.clone().add(Math.cos(a) * 1.5, y, Math.sin(a) * 1.5);
                Fx.p(s.world, Particle.SPLASH, l, 3, 0.4, 0.1);
                Fx.p(s.world, Particle.FALLING_WATER, l, 1, 0.5, 0);
            }
            for (Entity e : s.world.getNearbyEntities(c.clone().add(0, h / 2.0, 0), r, h / 2.0 + 2, r)) {
                if (!(e instanceof LivingEntity le) || s.spared(e)) continue;
                double above = le.getLocation().getY() - c.getY();
                Vector in = c.toVector().subtract(le.getLocation().toVector()).setY(0).multiply(0.1);
                le.setVelocity(in.setY(above < h ? 0.8 : 0.1));
            }
            if (t % 10 == 0) s.sound(c, "entity.generic.splash", 6f, 0.5f);
        });
    }
}
