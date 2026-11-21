package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.*;

/** Drains the life out of the land: plants die, water dries up, and it all turns to desert. */
public final class Desertify extends Cannon {
    public Desertify() {
        super("&e&lDesertify", Material.DEAD_BUSH, "Drains the land: plants die, water dries", "up, and it all turns to desert.");
        warning(30, "#E0C080");
        option("radius", 30, "Range.");
        option("thirst-seconds", 30, "Hunger effect for anyone caught in it.");
    }

    @Override
    public void strike(Strike s) {
        World w = s.world;
        Random rnd = new Random();
        s.blocks(Blocks.throttle(Blocks.columns(s.at(), i("radius"), (x, z, dist) -> {
            if (!Blocks.loaded(w, x, z)) return 1;
            int top = w.getHighestBlockYAt(x, z);
            int work = 1;
            for (int y = top; y > top - 6; y--) {
                Block b = w.getBlockAt(x, y, z);
                String n = b.getType().name();
                if (b.isLiquid() && b.getType() == Material.WATER || n.contains("LEAVES") || n.contains("FLOWER") || n.contains("GRASS") && !n.equals("GRASS_BLOCK")
                        || n.contains("FERN") || n.contains("VINE") || n.contains("SAPLING")) Blocks.set(b, Material.AIR);
                else if (b.getType() == Material.GRASS_BLOCK || b.getType() == Material.DIRT || b.getType() == Material.PODZOL || b.getType() == Material.MUD) {
                    Blocks.set(b, y >= top - 1 ? Material.SAND : Material.SANDSTONE);
                }
                work++;
            }
            Block surf = w.getBlockAt(x, w.getHighestBlockYAt(x, z) + 1, z);
            if (surf.getType().isAir() && w.getBlockAt(x, surf.getY() - 1, z).getType() == Material.SAND && rnd.nextInt(30) == 0) {
                Blocks.put(surf, rnd.nextInt(3) == 0 ? Material.CACTUS : Material.DEAD_BUSH, false);
            }
            return work;
        }), 2000));
        for (LivingEntity le : s.victims(s.at(), d("radius"))) le.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, i("thirst-seconds") * 20, 2));
        s.sound(s.at(), "block.sand.break", 10f, 0.5f);
    }
}
