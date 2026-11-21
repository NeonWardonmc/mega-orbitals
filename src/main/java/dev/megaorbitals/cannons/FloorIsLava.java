package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import java.util.*;

/** The floor is lava. Literally. The ground glows, then melts patch by patch. Find high ground. */
public final class FloorIsLava extends Cannon {
    public FloorIsLava() {
        super("&c&lThe Floor Is Lava", Material.LAVA_BUCKET, "Literally. The ground glows, then melts", "patch by patch. Find high ground.");
        warning(20, "#FF4010");
        option("radius", 16, "Range.");
        option("seconds", 30, "How long the melting goes on.");
        option("patches-per-second", 25, "Ground blocks that melt each second.");
    }

    @Override
    public void strike(Strike s) {
        if (!s.breaks()) return;
        s.every(10, i("seconds") * 2, t -> {
            for (int k = 0; k < i("patches-per-second") / 2; k++) {
                Block b = s.randomPoint(d("radius")).add(0, -1, 0).getBlock();
                if (!b.getType().isSolid() || !Blocks.breakable(b)) continue;
                Blocks.set(b, Material.MAGMA_BLOCK);
                s.later(40, () -> { if (b.getType() == Material.MAGMA_BLOCK) Blocks.put(b, Material.LAVA); });
            }
            Fx.p(s.world, Particle.LAVA, s.at(), 10, d("radius") / 2, 0);
            if (t % 4 == 0) s.sound(s.at(), "block.lava.pop", 6f, 0.6f);
        });
    }
}
