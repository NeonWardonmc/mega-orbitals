package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;

/** A laser that sweeps in a circle around the target, carving a burning trench. */
public final class Sweeper extends Cannon {
    public Sweeper() {
        super("&c&lSweeper", Material.BLAZE_ROD, "A laser sweeps a circle around the target,", "carving a burning trench.");
        warning(30, "#FF3030");
        option("radius", 20, "Radius of the sweep circle.");
        option("seconds", 8, "How long the sweep takes.");
        option("rotations", 2, "Full circles during that time.");
        option("damage", 10.0, "Damage to anything the beam touches.");
        option("trench-width", 2, "Half-width of the trench.");
    }

    @Override
    public void strike(Strike s) {
        int ticks = i("seconds") * 20;
        double r = d("radius");
        int tw = i("trench-width");
        Color c = Color.fromRGB(255, 50, 30);
        s.every(1, ticks, t -> {
            double a = 2 * Math.PI * i("rotations") * t / ticks;
            Location g = s.surface(s.at(Math.cos(a) * r, 0, Math.sin(a) * r));
            Fx.dustLine(s.world, g.clone().add(0, 60, 0), g, c, 2f, 1.0);
            Fx.p(s.world, Particle.FLAME, g, 5, 0.5, 0.05);
            for (LivingEntity le : s.victims(g, tw + 1.5)) { s.damage(le, d("damage")); le.setFireTicks(80); }
            if (s.breaks()) {
                for (int x = -tw; x <= tw; x++) for (int y = -3; y <= 1; y++) for (int z = -tw; z <= tw; z++) {
                    Blocks.set(g.clone().add(x, y, z).getBlock(), Material.AIR);
                }
                Block floor = g.clone().add(0, -4, 0).getBlock();
                if (floor.getType().isSolid()) Blocks.set(floor, Material.MAGMA_BLOCK);
            }
            if (t % 8 == 0) s.sound(g, "entity.blaze.shoot", 4f, 0.6f);
        });
    }
}
