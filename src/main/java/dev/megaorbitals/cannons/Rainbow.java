package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.block.Block;

/** Paints the ground in giant rainbow rings. Harmless, extremely visible from the sky. */
public final class Rainbow extends Cannon {
    public Rainbow() {
        super("&c&lR&6&la&e&li&a&ln&b&lb&9&lo&d&lw", Material.RED_WOOL, "Paints the ground in giant rainbow rings.", "Harmless. Extremely visible from the sky.");
        warning(10, "#FF80FF");
        option("radius", 28, "Size of the rainbow.");
        option("ring-width", 3, "Width of each colour ring.");
    }

    @Override
    public void strike(Strike s) {
        Material[] colors = {Material.RED_WOOL, Material.ORANGE_WOOL, Material.YELLOW_WOOL, Material.LIME_WOOL,
                Material.LIGHT_BLUE_WOOL, Material.BLUE_WOOL, Material.PURPLE_WOOL};
        World w = s.world;
        int rw = Math.max(1, i("ring-width"));
        s.blocks(Blocks.throttle(Blocks.columns(s.at(), i("radius"), (x, z, dist) -> {
            if (!Blocks.loaded(w, x, z)) return 1;
            Block top = s.surface(new Location(w, x + 0.5, s.at().getY(), z + 0.5)).getBlock().getRelative(0, -1, 0);
            if (!top.getType().isAir()) Blocks.set(top, colors[((int) dist / rw) % colors.length]);
            return 4;
        }), 500));
        s.sound(s.at(), "entity.player.levelup", 8f, 1.2f);
    }
}
