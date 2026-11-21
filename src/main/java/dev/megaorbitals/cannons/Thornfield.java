package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import java.util.*;

/** The ground sprouts a thick field of thorny berry bushes and cacti. Walking hurts. */
public final class Thornfield extends Cannon {
    public Thornfield() {
        super("&2&lThornfield", Material.SWEET_BERRIES, "The ground sprouts thick thorny bushes", "and cacti. Every step hurts.");
        warning(20, "#40A040");
        option("radius", 18, "Size of the field.");
        option("density", 0.6, "How much of the ground gets a bush (0-1).");
    }

    @Override
    public void strike(Strike s) {
        Random rnd = new Random();
        World w = s.world;
        s.blocks(Blocks.columns(s.at(), i("radius"), (x, z, dist) -> {
            if (!Blocks.loaded(w, x, z) || rnd.nextDouble() > d("density")) return 1;
            Location top = s.surface(new Location(w, x + 0.5, s.at().getY(), z + 0.5));
            Block ground = top.getBlock().getRelative(0, -1, 0), b = top.getBlock();
            if (!b.getType().isAir()) return 1;
            if (rnd.nextInt(5) == 0) {
                Blocks.set(ground, Material.SAND);
                Blocks.put(b, Material.CACTUS, false);
            } else {
                Blocks.set(ground, Material.GRASS_BLOCK);
                Blocks.put(b, Material.SWEET_BERRY_BUSH, false);
                if (b.getBlockData() instanceof Ageable a) { a.setAge(a.getMaximumAge()); Blocks.put(b, a, false); }
            }
            return 4;
        }));
        s.sound(s.at(), "block.sweet_berry_bush.place", 8f, 0.6f);
    }
}
