package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;

/** The ground beneath the target melts into a bubbling lake of lava. */
public final class LavaLake extends Cannon {
    public LavaLake() {
        super("&6&lLava Lake", Material.MAGMA_BLOCK, "The ground beneath the target melts into", "a bubbling lake of lava.");
        warning(30, "#FF6010");
        option("radius", 9, "Lake radius.");
        option("depth", 4, "Lake depth.");
    }

    @Override
    public void strike(Strike s) {
        int r = i("radius"), depth = i("depth");
        double r2 = r * r;
        int ty = s.at().getBlockY();
        s.blocks(Blocks.box(s.at(), r, Math.max(depth, 6), r, (b, dx, dy, dz) -> {
            if (dx * dx + dz * dz > r2) return;
            int y = b.getY();
            if (y >= ty) Blocks.set(b, Material.AIR);
            else if (y >= ty - depth) Blocks.set(b, Material.LAVA);
            else if (y == ty - depth - 1) Blocks.set(b, Material.MAGMA_BLOCK);
        }));
        s.every(10, 30, t -> Fx.p(s.world, Particle.LAVA, s.at(), 10, r / 2.0, 0));
        s.sound(s.at(), "block.lava.ambient", 10f, 0.5f);
    }
}
