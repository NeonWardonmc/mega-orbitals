package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;

/** A long shower of smaller meteors raining all over the area. */
public final class AsteroidBelt extends Cannon {
    public AsteroidBelt() {
        super("&7&lAsteroid Belt", Material.COBBLED_DEEPSLATE, "A long shower of burning rocks", "hammers the whole area.");
        warning(30, "#C08040");
        option("radius", 40, "Area covered.");
        option("seconds", 15, "How long the shower lasts.");
        option("rocks-per-second", 3, "Asteroids each second.");
        option("power", 4.0, "Explosion power of each asteroid.");
    }

    @Override
    public void strike(Strike s) {
        int every = Math.max(1, 20 / Math.max(1, i("rocks-per-second")));
        s.every(every, i("seconds") * 20 / every, t -> rock(s, s.randomPoint(d("radius"))));
    }

    private void rock(Strike s, Location target) {
        double a = Math.random() * Math.PI * 2;
        Location start = target.clone().add(Math.cos(a) * 30, 60, Math.sin(a) * 30);
        Material m = Math.random() < 0.5 ? Material.COBBLED_DEEPSLATE : Material.MAGMA_BLOCK;
        BlockDisplay rock = Fx.display(start, m, 1.5f + (float) Math.random() * 1.5f);
        s.every(1, 31, t -> {
            Location l = start.clone().add(target.toVector().subtract(start.toVector()).multiply(t / 30.0));
            if (t % 2 == 0) rock.teleport(l);
            Fx.p(s.world, Particle.FLAME, l, 5, 0.6, 0.02);
            Fx.p(s.world, Particle.SMOKE, l, 3, 0.6, 0.02);
            if (t == 30) { rock.remove(); s.explode(target, (float) d("power"), true); }
        });
    }
}
