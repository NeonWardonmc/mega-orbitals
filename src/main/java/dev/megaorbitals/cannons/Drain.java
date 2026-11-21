package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.block.data.Waterlogged;

/** Drains every drop of water and lava in range. Oceans, lakes, lava pools: gone. */
public final class Drain extends Cannon {
    public Drain() {
        super("&9&lDrain", Material.SPONGE, "Drains every drop of water and lava in range.", "Oceans, lakes, lava pools: gone.");
        warning(15, "#4080C0");
        option("radius", 30, "Range.");
        option("depth", 30, "How far down it drains.");
    }

    @Override
    public void strike(Strike s) {
        int r = i("radius"), depth = i("depth");
        int r2 = r * r;
        s.blocks(Blocks.box(s.at(0, -depth / 2.0 + 4, 0), r, depth / 2 + 4, r, (b, dx, dy, dz) -> {
            if (dx * dx + dz * dz > r2) return;
            Material m = b.getType();
            String n = m.name();
            if (b.isLiquid() || n.equals("KELP") || n.equals("KELP_PLANT") || n.equals("SEAGRASS") || n.equals("TALL_SEAGRASS") || n.equals("BUBBLE_COLUMN")) {
                Blocks.set(b, Material.AIR);
            } else if (b.getBlockData() instanceof Waterlogged w && w.isWaterlogged()) {
                w.setWaterlogged(false);
                b.setBlockData(w, false);
            }
        }));
        s.sound(s.at(), "block.sponge.absorb", 10f, 0.6f);
    }
}
