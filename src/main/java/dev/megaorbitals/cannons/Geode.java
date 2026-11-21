package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import java.util.*;

/** Seals the area inside a giant amethyst geode, glittering with crystals on the inside. */
public final class Geode extends Cannon {
    public Geode() {
        super("&d&lGeode Prison", Material.AMETHYST_BLOCK, "Seals the area inside a giant amethyst", "geode, glittering with crystals inside.");
        warning(25, "#C080FF");
        option("radius", 11, "Geode radius.");
    }

    @Override
    public void strike(Strike s) {
        double r = d("radius");
        int ir = (int) Math.ceil(r);
        Random rnd = new Random();
        s.blocks(Blocks.box(s.at(), ir, ir, ir, (b, dx, dy, dz) -> {
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (dist > r) return;
            if (dist > r - 1) Blocks.set(b, Material.SMOOTH_BASALT);
            else if (dist > r - 2) Blocks.set(b, Material.CALCITE);
            else if (dist > r - 3) Blocks.set(b, rnd.nextInt(8) == 0 ? Material.BUDDING_AMETHYST : Material.AMETHYST_BLOCK);
            else if (dist > r - 4 && b.getType().isAir() && rnd.nextInt(6) == 0) Blocks.set(b, Material.AMETHYST_CLUSTER);
        }));
        s.sound(s.at(), "block.amethyst_block.resonate", 10f, 0.6f);
    }
}
