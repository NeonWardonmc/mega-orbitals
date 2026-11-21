package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;

/** X-ray strike: turns every solid block in a sphere into glass. Hidden bases, revealed. */
public final class Glassify extends Cannon {
    public Glassify() {
        super("&b&lX-Ray Strike", Material.GLASS, "Turns every solid block in a sphere into glass.", "Hidden bases and stashes, revealed.");
        warning(20, "#C0F0FF");
        option("radius", 14, "Sphere radius.");
        option("keep-ores-and-containers", true, "Leave ores and storage blocks visible as they are.");
    }

    @Override
    public void strike(Strike s) {
        int r = i("radius");
        double r2 = r * r;
        boolean keep = b("keep-ores-and-containers");
        s.blocks(Blocks.box(s.at(0, -r / 2.0, 0), r, r, r, (b, dx, dy, dz) -> {
            if (dx * dx + dy * dy + dz * dz > r2 || !b.getType().isSolid()) return;
            String n = b.getType().name();
            if (keep && (n.endsWith("_ORE") || n.contains("CHEST") || n.contains("BARREL") || n.contains("SHULKER") || n.contains("FURNACE")
                    || n.contains("HOPPER") || n.contains("SPAWNER") || n.equals("ANCIENT_DEBRIS"))) return;
            Blocks.set(b, Material.GLASS);
        }));
        s.sound(s.at(), "block.glass.place", 10f, 0.6f);
    }
}
