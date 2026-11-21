package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import java.util.*;

/** Ages everything by a thousand years: bricks crack, stone gets mossy, windows break, cobwebs appear. */
public final class Ruinify extends Cannon {
    public Ruinify() {
        super("&7&lThousand Years", Material.MOSSY_COBBLESTONE, "Ages everything by a thousand years: bricks crack,", "stone gets mossy, windows break, cobwebs appear.");
        warning(20, "#708060");
        option("radius", 20, "Range.");
        option("decay", 0.08, "Chance each block crumbles away entirely.");
    }

    @Override
    public void strike(Strike s) {
        Random rnd = new Random();
        int r = i("radius");
        double r2 = r * r;
        s.blocks(Blocks.throttle(Blocks.box(s.at(0, 6, 0), r, 14, r, (b, dx, dy, dz) -> {
            if (dx * dx + dy * dy + dz * dz > r2 * 1.5) return;
            Material m = b.getType();
            String n = m.name();
            if (m.isAir()) {
                if (rnd.nextInt(300) == 0 && b.getRelative(0, 1, 0).getType().isSolid()) Blocks.put(b, Material.COBWEB, false);
                return;
            }
            if (n.contains("GLASS")) { Blocks.set(b, Material.AIR); return; }
            if (m.isSolid() && rnd.nextDouble() < d("decay") && dy > 0) { Blocks.set(b, Material.AIR); return; }
            switch (m) {
                case STONE_BRICKS -> Blocks.set(b, rnd.nextBoolean() ? Material.CRACKED_STONE_BRICKS : Material.MOSSY_STONE_BRICKS);
                case COBBLESTONE -> { if (rnd.nextBoolean()) Blocks.set(b, Material.MOSSY_COBBLESTONE); }
                case DEEPSLATE_BRICKS -> Blocks.set(b, Material.CRACKED_DEEPSLATE_BRICKS);
                case POLISHED_BLACKSTONE_BRICKS -> Blocks.set(b, Material.CRACKED_POLISHED_BLACKSTONE_BRICKS);
                case NETHER_BRICKS -> Blocks.set(b, Material.CRACKED_NETHER_BRICKS);
                case GRASS_BLOCK -> { if (rnd.nextInt(4) == 0) Blocks.set(b, Material.COARSE_DIRT); }
                default -> { if (n.endsWith("_PLANKS") && rnd.nextInt(5) == 0) Blocks.set(b, Material.AIR); }
            }
        }), 1500));
        s.sound(s.at(), "ambient.cave", 8f, 0.6f);
    }
}
