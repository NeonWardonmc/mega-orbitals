package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;

/** Buries every target under a tower of falling sand. */
public final class SandTomb extends Cannon {
    public SandTomb() {
        super("&e&lSand Tomb", Material.SAND, "Buries every target under", "a tower of falling sand.");
        warning(20, "#E0D080");
        option("radius", 20, "Range.");
        option("tower-height", 10, "Height of each sand tower.");
        option("material", "SAND", "Block to bury them with (SAND, GRAVEL, ...).");
    }

    @Override
    public void strike(Strike s) {
        Material m = mat("material");
        for (LivingEntity le : s.victims(s.at(), d("radius"))) {
            Location base = le.getLocation().getBlock().getLocation().add(0.5, 0, 0.5);
            int h = i("tower-height");
            s.every(2, h, t -> {
                for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++) {
                    s.fallingBlock(base.clone().add(x, 6 + t, z), m, !s.breaks());
                }
            });
        }
        s.sound(s.at(), "block.sand.fall", 10f, 0.5f);
    }
}
