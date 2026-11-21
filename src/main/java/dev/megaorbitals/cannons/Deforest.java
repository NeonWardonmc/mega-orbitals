package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;

/** Clear-cuts every tree in range: logs, leaves, mushrooms, vines, all gone in seconds. */
public final class Deforest extends Cannon {
    public Deforest() {
        super("&6&lClear-Cut", Material.IRON_AXE, "Clear-cuts every tree in range: logs, leaves,", "mushrooms and vines, all gone in seconds.");
        warning(15, "#A07030");
        option("radius", 35, "Range.");
        option("height", 40, "Vertical range.");
    }

    private static boolean tree(Material m) {
        String n = m.name();
        return n.endsWith("_LOG") || n.endsWith("_LEAVES") || n.endsWith("_WOOD") || n.endsWith("_STEM") || n.endsWith("_HYPHAE")
                || n.equals("VINE") || n.equals("MANGROVE_ROOTS") || n.endsWith("MUSHROOM_BLOCK") || n.equals("BEE_NEST") || n.equals("COCOA");
    }

    @Override
    public void strike(Strike s) {
        int r = i("radius");
        int r2 = r * r;
        s.blocks(Blocks.box(s.at(0, i("height") / 2.0 - 4, 0), r, i("height") / 2, r, (b, dx, dy, dz) -> {
            if (dx * dx + dz * dz <= r2 && tree(b.getType())) Blocks.set(b, Material.AIR);
        }));
        s.sound(s.at(), "block.wood.break", 10f, 0.6f);
    }
}
