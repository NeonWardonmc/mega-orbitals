package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import java.util.*;

/** Seals the area in a giant glass snow globe, with snow drifting down inside it forever. */
public final class SnowGlobe extends Cannon {
    public SnowGlobe() {
        super("&f&lSnow Globe", Material.SNOWBALL, "Seals the area in a giant glass snow globe,", "with snow drifting down inside it.");
        warning(15, "#E0F0FF");
        option("radius", 14, "Globe radius.");
        option("snow-seconds", 300, "How long it snows inside.");
    }

    @Override
    public void strike(Strike s) {
        int r = i("radius");
        Random rnd = new Random();
        s.blocks(Blocks.box(s.at(0, -1, 0), r, r, r, (b, dx, dy, dz) -> {
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (dy >= 0 && dist <= r && dist > r - 1.2 && !b.getType().isSolid()) Blocks.set(b, Material.GLASS);
            if (dy == 0 && dist < r - 1 && b.getType().isAir() && rnd.nextInt(3) == 0) b.setType(Material.SNOW, false);
        }));
        s.every(5, i("snow-seconds") * 4, t -> Fx.p(s.world, Particle.SNOWFLAKE, s.at(0, r * 0.7, 0), 25, r / 2.5, 0.02));
        s.sound(s.at(), "block.glass.place", 8f, 1.2f);
    }
}
