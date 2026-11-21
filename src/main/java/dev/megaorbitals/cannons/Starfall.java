package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import java.util.*;

/** Shooting stars streak down all over the area, each leaving a glowing crater. */
public final class Starfall extends Cannon {
    public Starfall() {
        super("&e&lStarfall", Material.NETHER_STAR, "Shooting stars streak down all over the area,", "each leaving a small glowing crater.");
        warning(30, "#FFF080");
        option("radius", 30, "Area covered.");
        option("seconds", 15, "How long it lasts.");
        option("stars-per-second", 4, "Stars each second.");
        option("power", 1.8, "Impact power of each star.");
    }

    @Override
    public void strike(Strike s) {
        Random rnd = new Random();
        int every = Math.max(1, 20 / Math.max(1, i("stars-per-second")));
        s.every(every, i("seconds") * 20 / every, t -> {
            Location hit = s.randomPoint(d("radius"));
            double a = rnd.nextDouble() * Math.PI * 2;
            Location from = hit.clone().add(Math.cos(a) * 25, 50, Math.sin(a) * 25);
            s.every(1, 8, k -> {
                Location p = from.clone().add(hit.toVector().subtract(from.toVector()).multiply((k + 1) / 8.0));
                Fx.p(s.world, Particle.END_ROD, p, 4, 0.1, 0.02);
                Fx.p(s.world, Particle.FIREWORK, p, 2, 0.1, 0.02);
                if (k != 7) return;
                s.explode(hit, (float) d("power"), false);
                Block b = hit.getBlock();
                if (s.breaks() && rnd.nextInt(3) == 0 && b.getType().isAir()) b.setType(Material.GLOWSTONE, false);
            });
        });
    }
}
