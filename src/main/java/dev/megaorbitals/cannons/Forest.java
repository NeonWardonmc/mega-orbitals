package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import java.util.*;

/** A whole forest erupts out of the ground in seconds, trapping anyone inside. */
public final class Forest extends Cannon {
    public Forest() {
        super("&2&lInstant Forest", Material.OAK_SAPLING, "A whole forest erupts out of the ground", "in seconds. Good luck getting out.");
        warning(20, "#208020");
        option("radius", 25, "Area of the forest.");
        option("trees", 50, "Number of trees.");
    }

    private static final TreeType[] TREES = {TreeType.TREE, TreeType.BIG_TREE, TreeType.BIRCH, TreeType.TALL_BIRCH, TreeType.REDWOOD,
            TreeType.TALL_REDWOOD, TreeType.MEGA_REDWOOD, TreeType.SMALL_JUNGLE, TreeType.ACACIA, TreeType.CHERRY, TreeType.AZALEA};

    @Override
    public void strike(Strike s) {
        if (!s.breaks()) return;
        Random rnd = new Random();
        s.blocks(Blocks.surface(s.at(), i("radius"), Material.GRASS_BLOCK));
        s.every(1, i("trees"), t -> {
            Location l = s.randomPoint(d("radius"));
            s.world.generateTree(l, TREES[rnd.nextInt(TREES.length)]);
            Fx.p(s.world, Particle.HAPPY_VILLAGER, l.add(0, 1, 0), 15, 1, 0);
            if (t % 5 == 0) s.sound(l, "block.grass.place", 4f, 0.5f);
        });
    }
}
