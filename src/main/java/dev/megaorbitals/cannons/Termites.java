package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;

/** A termite infestation eats every wooden block in range: houses, floors, doors, fences. */
public final class Termites extends Cannon {
    public Termites() {
        super("&6&lTermites", Material.OAK_PLANKS, "A termite infestation eats every wooden block:", "houses, floors, doors, fences, chests' tables.");
        warning(20, "#A07040");
        option("radius", 20, "Range.");
        option("height", 30, "Vertical range.");
        option("eat-speed", 150, "Blocks checked per tick (lower = slower munching).");
    }

    private static boolean wooden(Material m) {
        String n = m.name();
        if (n.contains("CHEST") || n.contains("BARREL") || n.contains("SIGN")) return false;
        return n.contains("OAK") || n.contains("SPRUCE") || n.contains("BIRCH") || n.contains("JUNGLE") || n.contains("ACACIA")
                || n.contains("MANGROVE") || n.contains("CHERRY") || n.contains("BAMBOO") || n.contains("CRIMSON") || n.contains("WARPED")
                || n.equals("CRAFTING_TABLE") || n.equals("BOOKSHELF") || n.equals("LADDER") || n.equals("COMPOSTER") || n.equals("LECTERN");
    }

    @Override
    public void strike(Strike s) {
        int r = i("radius"), h = i("height");
        int r2 = r * r;
        s.blocks(Blocks.throttle(Blocks.box(s.at(0, h / 2.0 - 5, 0), r, h / 2, r, (b, dx, dy, dz) -> {
            if (dx * dx + dz * dz > r2 || !wooden(b.getType())) return;
            Fx.block(s.world, Particle.BLOCK, b.getLocation().add(0.5, 0.5, 0.5), b.getType(), 3, 0.3);
            Blocks.set(b, Material.AIR);
        }), i("eat-speed")));
        s.every(20, 20, t -> s.sound(s.at(), "block.wood.hit", 6f, 0.5f));
    }
}
