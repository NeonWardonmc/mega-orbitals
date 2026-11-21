package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import java.util.*;

/** The floor becomes a patchwork of magma and soul fire. There's nowhere safe to stand. */
public final class Hellfloor extends Cannon {
    public Hellfloor() {
        super("&4&lHellfloor", Material.SOUL_CAMPFIRE, "The floor becomes magma and soul fire.", "There's nowhere safe to stand.");
        warning(20, "#C03010");
        option("radius", 16, "Size of the floor.");
        option("soul-fire-chance", 0.3, "Chance of soul fire on each spot (0-1).");
    }

    @Override
    public void strike(Strike s) {
        World w = s.world;
        Random rnd = new Random();
        s.blocks(Blocks.columns(s.at(), i("radius"), (x, z, dist) -> {
            if (!Blocks.loaded(w, x, z)) return 1;
            Block ground = s.surface(new Location(w, x + 0.5, s.at().getY(), z + 0.5)).getBlock().getRelative(0, -1, 0);
            if (rnd.nextDouble() < d("soul-fire-chance")) {
                Blocks.set(ground, Material.SOUL_SOIL);
                Block up = ground.getRelative(0, 1, 0);
                if (up.getType().isAir()) Blocks.put(up, Material.SOUL_FIRE, false);
            } else Blocks.set(ground, Material.MAGMA_BLOCK);
            return 4;
        }));
        Fx.p(w, Particle.SOUL_FIRE_FLAME, s.at(0, 1, 0), 200, d("radius") / 2, 0.02);
        s.sound(s.at(), "block.soul_sand.place", 10f, 0.5f);
    }
}
