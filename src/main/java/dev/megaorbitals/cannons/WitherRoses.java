package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import java.util.*;

/** Plants a cursed garden of wither roses. Touch one and you start to wither. */
public final class WitherRoses extends Cannon {
    public WitherRoses() {
        super("&8&lCursed Garden", Material.WITHER_ROSE, "Plants a cursed garden of wither roses.", "Touch one and you start to wither.");
        warning(15, "#303030");
        option("radius", 16, "Size of the garden.");
        option("density", 0.5, "How much of the ground gets a rose (0-1).");
    }

    @Override
    public void strike(Strike s) {
        World w = s.world;
        Random rnd = new Random();
        s.blocks(Blocks.columns(s.at(), i("radius"), (x, z, dist) -> {
            if (!Blocks.loaded(w, x, z) || rnd.nextDouble() > d("density")) return 1;
            Block ground = w.getHighestBlockAt(x, z);
            Block above = ground.getRelative(0, 1, 0);
            if (!above.getType().isAir() || !ground.getType().isSolid()) return 1;
            Blocks.set(ground, Material.SOUL_SOIL);
            above.setType(Material.WITHER_ROSE, false);
            return 3;
        }));
        Fx.p(w, Particle.SOUL, s.at(0, 1, 0), 100, d("radius") / 2, 0.02);
        s.sound(s.at(), "entity.wither.ambient", 6f, 1.5f);
    }
}
