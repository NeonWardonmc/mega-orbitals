package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;

/** Drops an instant reinforced bunker at the target: obsidian walls, a door gap, lights inside. */
public final class Bunker extends Cannon {
    public Bunker() {
        super("&8&lInstant Bunker", Material.OBSIDIAN, "Drops a reinforced obsidian bunker", "at the target, with a door and lights.");
        warning(10, "#505050");
        option("half-size", 5, "Half the width of the bunker.");
        option("height", 5, "Inside height.");
        option("material", "OBSIDIAN", "Wall material.");
    }

    @Override
    public void strike(Strike s) {
        int hs = i("half-size"), h = i("height");
        Material wall = mat("material");
        Location c = s.at();
        s.blocks(Blocks.box(c.clone().add(0, h / 2.0, 0), hs, h / 2 + 1, hs, (b, dx, dy, dz) -> {
            int y = b.getY() - c.getBlockY();
            boolean edge = Math.abs(dx) == hs || Math.abs(dz) == hs || y == -1 || y == h;
            boolean door = dz == -hs && dx == 0 && (y == 0 || y == 1);
            if (y < -1 || y > h) return;
            if (door) Blocks.set(b, Material.AIR);
            else if (edge) Blocks.set(b, (y == h && dx % 3 == 0 && dz % 3 == 0) ? Material.GLOWSTONE : wall);
            else Blocks.set(b, Material.AIR);
        }));
        s.sound(c, "block.anvil.place", 6f, 0.6f);
    }
}
