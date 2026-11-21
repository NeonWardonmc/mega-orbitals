package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;

/** Floods the area into a sea and releases elder guardians and their guardian army. */
public final class Kraken extends Cannon {
    public Kraken() {
        super("&3&lRelease the Kraken", Material.PRISMARINE_SHARD, "Floods the area into a sea and releases", "elder guardians with a guardian army.");
        warning(40, "#208080");
        option("radius", 14, "Size of the sea.");
        option("depth", 8, "Depth of the sea.");
        option("elders", 2, "Elder guardians.");
        option("guardians", 8, "Guardians.");
    }

    @Override
    public void strike(Strike s) {
        int r = i("radius"), depth = i("depth");
        int r2 = r * r;
        s.blocks(Blocks.box(s.at(0, depth / 2.0 - 1, 0), r, depth / 2, r, (b, dx, dy, dz) -> {
            if (dx * dx + dz * dz <= r2 && (b.getType().isAir() || !b.getType().isSolid())) Blocks.set(b, Material.WATER);
        }));
        s.later(10, () -> {
            for (int k = 0; k < i("elders"); k++) s.hunt(s.mob(s.at(Math.random() * 6 - 3, 3, Math.random() * 6 - 3), ElderGuardian.class, null), 40);
            for (int k = 0; k < i("guardians"); k++) s.hunt(s.mob(s.at(Math.random() * r - r / 2.0, 2, Math.random() * r - r / 2.0), Guardian.class, null), 40);
            s.sound(s.at(), "entity.elder_guardian.curse", 10f, 0.6f);
        });
    }
}
