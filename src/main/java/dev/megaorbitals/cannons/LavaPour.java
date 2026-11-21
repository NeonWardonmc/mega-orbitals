package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import java.util.*;

/** Waterfalls of lava pour down from the sky onto the target. */
public final class LavaPour extends Cannon {
    public LavaPour() {
        super("&6&lLava Falls", Material.LAVA_BUCKET, "Waterfalls of lava pour down", "from the sky onto the target.");
        warning(30, "#FF7000");
        option("streams", 8, "Number of lava streams.");
        option("radius", 8, "Spread of the streams.");
        option("height", 25, "Height the lava pours from.");
        option("seconds", 20, "How long the lava pours.");
    }

    @Override
    public void strike(Strike s) {
        if (!s.breaks()) return;
        List<Block> sources = new ArrayList<>();
        sources.add(s.at(0, i("height"), 0).getBlock());
        for (Location l : Fx.circle(s.at(0, i("height"), 0), d("radius"), i("streams"))) sources.add(l.getBlock());
        for (Block b : sources) if (b.getType().isAir()) b.setType(Material.LAVA);
        s.every(10, i("seconds") * 2, t -> {
            for (Block b : sources) Fx.p(s.world, Particle.DRIPPING_LAVA, b.getLocation().add(0.5, 0, 0.5), 3, 0.3, 0);
            if (t % 4 == 0) s.sound(s.at(), "block.lava.ambient", 8f, 0.5f);
        });
        s.later(i("seconds") * 20L, () -> {
            for (Block b : sources) if (b.getType() == Material.LAVA) b.setType(Material.AIR);
        });
    }
}
