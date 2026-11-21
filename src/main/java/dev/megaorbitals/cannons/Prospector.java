package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import java.util.*;

/** Support for miners: turns the stone underground into a seam packed with ores. */
public final class Prospector extends Cannon {
    public Prospector() {
        super("&b&lProspector", Material.DIAMOND_ORE, "Support for miners: turns the stone", "underground into a seam packed with ores.");
        warning(15, "#60E0E0");
        option("radius", 10, "Sphere radius.");
        option("ore-chance", 0.25, "Chance each stone block becomes an ore.");
    }

    @Override
    public void strike(Strike s) {
        Random rnd = new Random();
        Material[] stone = {Material.COAL_ORE, Material.COAL_ORE, Material.IRON_ORE, Material.IRON_ORE, Material.COPPER_ORE,
                Material.GOLD_ORE, Material.REDSTONE_ORE, Material.LAPIS_ORE, Material.DIAMOND_ORE, Material.EMERALD_ORE};
        Material[] deep = {Material.DEEPSLATE_COAL_ORE, Material.DEEPSLATE_IRON_ORE, Material.DEEPSLATE_IRON_ORE, Material.DEEPSLATE_GOLD_ORE,
                Material.DEEPSLATE_REDSTONE_ORE, Material.DEEPSLATE_LAPIS_ORE, Material.DEEPSLATE_DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE};
        int r = i("radius");
        double r2 = r * r;
        s.blocks(Blocks.box(s.at(0, -r, 0), r, r, r, (b, dx, dy, dz) -> {
            if (dx * dx + dy * dy + dz * dz > r2 || rnd.nextDouble() > d("ore-chance")) return;
            Material m = b.getType();
            if (m == Material.STONE || m == Material.ANDESITE || m == Material.GRANITE || m == Material.DIORITE) Blocks.set(b, stone[rnd.nextInt(stone.length)]);
            else if (m == Material.DEEPSLATE || m == Material.TUFF) Blocks.set(b, deep[rnd.nextInt(deep.length)]);
        }));
        s.sound(s.at(), "block.amethyst_block.chime", 8f, 0.8f);
    }
}
