package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;

/** Inverts reality in a sphere: air becomes stone and stone becomes air. */
public final class Inverter extends Cannon {
    public Inverter() {
        super("&5&lInverter", Material.OBSERVER, "Inverts reality in a sphere: air becomes", "solid and solid becomes air.");
        warning(25, "#8040C0");
        option("radius", 9, "Sphere radius.");
        option("fill", "STONE", "What empty space turns into.");
    }

    @Override
    public void strike(Strike s) {
        int r = i("radius");
        double r2 = r * r;
        Material fill = mat("fill");
        s.blocks(Blocks.box(s.at(), r, r, r, (b, dx, dy, dz) -> {
            if (dx * dx + dy * dy + dz * dz > r2) return;
            if (b.getType().isAir() || b.isLiquid()) Blocks.set(b, fill);
            else Blocks.set(b, Material.AIR);
        }));
        s.sound(s.at(), "block.respawn_anchor.deplete", 8f, 0.5f);
    }
}
