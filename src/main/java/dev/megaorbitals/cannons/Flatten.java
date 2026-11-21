package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.block.Block;

/** Levels everything in range to perfectly flat ground at the target's height. */
public final class Flatten extends Cannon {
    public Flatten() {
        super("&a&lFlattener", Material.SMOOTH_STONE_SLAB, "Levels everything in range to perfectly", "flat ground at the target's height.");
        warning(40, "#80C080");
        option("radius", 40, "Range.");
        option("fill-holes", true, "Fill dips and holes up to the level.");
        option("max-height", 120, "How high above the level it clears.");
    }

    @Override
    public void strike(Strike s) {
        World w = s.world;
        int level = s.at().getBlockY() - 1;
        boolean fill = b("fill-holes");
        int maxH = i("max-height");
        s.blocks(Blocks.columns(s.at(), i("radius"), (x, z, dist) -> {
            if (!Blocks.loaded(w, x, z)) return 1;
            int top = Math.min(w.getHighestBlockYAt(x, z), level + maxH);
            int work = 1;
            for (int y = top; y > level; y--) { Block b = w.getBlockAt(x, y, z); if (!b.getType().isAir()) Blocks.set(b, Material.AIR); work++; }
            if (fill) {
                for (int y = level - 4; y < level; y++) {
                    Block b = w.getBlockAt(x, y, z);
                    if (b.getType().isAir() || b.isLiquid()) Blocks.set(b, Material.DIRT);
                    work++;
                }
            }
            Block surface = w.getBlockAt(x, level, z);
            if (fill || !surface.getType().isAir()) Blocks.set(surface, Material.GRASS_BLOCK);
            return work;
        }));
        s.sound(s.at(), "block.grass.place", 10f, 0.5f);
    }
}
