package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import java.util.*;

/** Tears open a rift to the Nether: the land turns hellish and Nether mobs pour out. */
public final class NetherRift extends Cannon {
    public NetherRift() {
        super("&4&lNether Rift", Material.NETHERRACK, "Tears open a rift to the Nether.", "The land burns and Nether mobs pour out.");
        warning(40, "#A01010");
        option("radius", 25, "Size of the rift.");
        option("piglins", 8, "Piglin brutes.");
        option("blazes", 4, "Blazes.");
        option("ghasts", 1, "Ghasts.");
    }

    @Override
    public void strike(Strike s) {
        World w = s.world;
        Random rnd = new Random();
        int r = i("radius");
        Material[] nether = {Material.NETHERRACK, Material.NETHERRACK, Material.NETHERRACK, Material.SOUL_SAND,
                Material.MAGMA_BLOCK, Material.CRIMSON_NYLIUM, Material.WARPED_NYLIUM, Material.BLACKSTONE};
        s.blocks(Blocks.throttle(Blocks.columns(s.at(), r, (x, z, dist) -> {
            if (!Blocks.loaded(w, x, z)) return 1;
            Location top = s.surface(new Location(w, x + 0.5, s.at().getY(), z + 0.5));
            Block g = top.getBlock().getRelative(0, -1, 0);
            if (rnd.nextInt(40) == 0) Blocks.set(g, Material.LAVA);
            else Blocks.set(g, nether[rnd.nextInt(nether.length)]);
            if (rnd.nextInt(15) == 0 && top.getBlock().getType().isAir()) top.getBlock().setType(Material.FIRE);
            return 4;
        }), 400));
        s.later(20, () -> {
            for (int k = 0; k < i("piglins"); k++) s.hunt(s.mob(s.randomPoint(r / 2.0), PiglinBrute.class, p -> p.setImmuneToZombification(true)), 40);
            for (int k = 0; k < i("blazes"); k++) s.hunt(s.mob(s.randomPoint(r / 2.0).add(0, 3, 0), Blaze.class, null), 40);
            for (int k = 0; k < i("ghasts"); k++) s.hunt(s.mob(s.at(0, 15, 0), Ghast.class, null), 60);
            s.soundWorld("ambient.nether_wastes.mood", 1f, 0.5f);
        });
        s.every(4, 100, t -> Fx.p(w, Particle.CRIMSON_SPORE, s.at(0, 3, 0), 40, r / 2.0, 0));
    }
}
