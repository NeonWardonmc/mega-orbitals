package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;

/** Infests the stone underground with silverfish. Every block you mine might bite back. */
public final class Infestation extends Cannon {
    public Infestation() {
        super("&7&lInfestation", Material.INFESTED_STONE, "Infests the stone underground with silverfish.", "Every block you mine might bite back.");
        warning(20, "#909090");
        option("radius", 16, "Radius of the infested area.");
        option("silverfish", 12, "Silverfish released right away.");
    }

    private static Material infested(Material m) {
        return switch (m) {
            case STONE -> Material.INFESTED_STONE;
            case COBBLESTONE -> Material.INFESTED_COBBLESTONE;
            case STONE_BRICKS -> Material.INFESTED_STONE_BRICKS;
            case MOSSY_STONE_BRICKS -> Material.INFESTED_MOSSY_STONE_BRICKS;
            case CRACKED_STONE_BRICKS -> Material.INFESTED_CRACKED_STONE_BRICKS;
            case DEEPSLATE -> Material.INFESTED_DEEPSLATE;
            default -> null;
        };
    }

    @Override
    public void strike(Strike s) {
        int r = i("radius");
        double r2 = r * r;
        s.blocks(Blocks.box(s.at(), r, r, r, (b, dx, dy, dz) -> {
            if (dx * dx + dy * dy + dz * dz > r2) return;
            Material m = infested(b.getType());
            if (m != null) Blocks.set(b, m);
        }));
        for (int k = 0; k < i("silverfish"); k++) s.hunt(s.mob(s.randomPoint(r / 2.0), Silverfish.class, null), 30);
        s.sound(s.at(), "entity.silverfish.ambient", 8f, 0.6f);
    }
}
