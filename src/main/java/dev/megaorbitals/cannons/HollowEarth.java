package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;

/** Hollows out everything inside a huge sphere but leaves the outer shell. From outside, nothing looks different. */
public final class HollowEarth extends Cannon {
    public HollowEarth() {
        super("&8&lHollow Earth", Material.STRUCTURE_VOID, "Hollows out everything inside a huge sphere,", "leaving only the shell. Outside looks normal.");
        warning(30, "#404040");
        option("radius", 18, "Sphere radius.");
        option("shell", 2.0, "Thickness of the shell left behind.");
    }

    @Override
    public void strike(Strike s) {
        double r = d("radius"), inner = r - d("shell");
        double in2 = inner * inner;
        int ir = (int) Math.ceil(r);
        s.blocks(Blocks.box(s.at(), ir, ir, ir, (b, dx, dy, dz) -> {
            if (dx * dx + dy * dy + dz * dz <= in2 && !b.getType().isAir()) Blocks.set(b, Material.AIR);
        }));
        s.sound(s.at(), "block.portal.ambient", 8f, 0.3f);
    }
}
