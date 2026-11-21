package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.block.Block;

/** Digs a ring-shaped moat of lava around the target. Perfect for trapping a base (or defending one). */
public final class LavaMoat extends Cannon {
    public LavaMoat() {
        super("&6&lLava Moat", Material.LAVA_BUCKET, "Digs a ring-shaped moat of lava around", "the target. Trap a base, or defend yours.");
        warning(30, "#FF8020");
        option("radius", 16, "Radius of the moat ring.");
        option("width", 3, "Width of the moat.");
        option("depth", 4, "Depth of the moat.");
    }

    @Override
    public void strike(Strike s) {
        int r = i("radius"), w = i("width"), depth = i("depth");
        World world = s.world;
        s.blocks(Blocks.columns(s.at(), r + w, (x, z, dist) -> {
            if (dist < r - w / 2.0 || dist > r + w / 2.0 || !Blocks.loaded(world, x, z)) return 1;
            Location top = s.surface(new Location(world, x + 0.5, s.at().getY(), z + 0.5));
            for (int y = 1; y <= depth; y++) {
                Block b = top.clone().add(0, -y, 0).getBlock();
                Blocks.set(b, y == depth ? Material.MAGMA_BLOCK : Material.LAVA);
            }
            for (int y = 0; y < 4; y++) Blocks.set(top.clone().add(0, y, 0).getBlock(), Material.AIR);
            return depth + 4;
        }));
        s.sound(s.at(), "block.lava.pop", 8f, 0.5f);
    }
}
