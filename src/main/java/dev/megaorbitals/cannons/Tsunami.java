package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;
import java.util.*;

/** A wall of water surges outward from the impact, sweeping everything away. */
public final class Tsunami extends Cannon {
    public Tsunami() {
        super("&9&lTsunami", Material.HEART_OF_THE_SEA, "A wall of water surges outward", "and sweeps everything away.");
        warning(30, "#2060FF");
        option("radius", 40, "How far the wave travels.");
        option("wave-height", 4, "Height of the water wall.");
        option("push", 1.4, "How hard the wave pushes.");
        option("leave-water", false, "Leave the water behind instead of draining it.");
    }

    @Override
    public void strike(Strike s) {
        Location c = s.at();
        int h = i("wave-height");
        List<Block> placed = new ArrayList<>();
        s.sound(c, "entity.generic.splash", 10f, 0.5f);
        s.every(2, i("radius"), t -> {
            double r = t + 2;
            List<Block> ring = new ArrayList<>();
            for (Location l : Fx.circle(c, r, (int) (r * 6))) {
                Location g = s.surface(l);
                Fx.p(s.world, Particle.SPLASH, g.clone().add(0, h, 0), 4, 0.5, 0.1);
                Fx.p(s.world, Particle.FALLING_WATER, g.clone().add(0, h, 0), 2, 0.5, 0);
                if (!s.breaks()) continue;
                for (int y = 0; y < h; y++) {
                    Block b = g.clone().add(0, y, 0).getBlock();
                    if (b.getType().isAir()) { Blocks.put(b, Material.WATER, false); ring.add(b); }
                }
            }
            placed.addAll(ring);
            for (LivingEntity le : s.victims(c, r + 2)) {
                if (le.getLocation().distance(c) < r - 2) continue;
                Vector out = Fx.dir(c, le.getLocation()).setY(0);
                le.setVelocity(out.multiply(d("push")).setY(0.4));
            }
            if (!b("leave-water")) s.later(10, () -> ring.forEach(b -> { if (b.getType() == Material.WATER) Blocks.put(b, Material.AIR, false); }));
            if (t % 5 == 0) s.sound(c.clone().add(r, 0, 0), "ambient.underwater.enter", 6f, 0.5f);
        });
    }
}
