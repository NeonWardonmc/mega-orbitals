package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

/** Explosive plant growth: the area is buried in moss, grass, flowers and crops grow instantly. */
public final class Overgrowth extends Cannon {
    public Overgrowth() {
        super("&a&lOvergrowth", Material.BONE_MEAL, "Explosive plant growth: moss, grass and", "flowers everywhere, and crops grow instantly.");
        warning(15, "#60E060");
        option("radius", 20, "Range.");
        option("moss", true, "Turn stone and dirt into moss.");
    }

    @Override
    public void strike(Strike s) {
        World w = s.world;
        s.blocks(Blocks.throttle(Blocks.columns(s.at(), i("radius"), (x, z, dist) -> {
            if (!Blocks.loaded(w, x, z)) return 1;
            Block top = w.getHighestBlockAt(x, z);
            Material t = top.getType();
            if (b("moss") && (t == Material.STONE || t == Material.DIRT || t == Material.COBBLESTONE || t == Material.COARSE_DIRT || t == Material.GRAVEL)) {
                Blocks.set(top, Material.MOSS_BLOCK);
            }
            top.applyBoneMeal(BlockFace.UP);
            return 6;
        }), 300));
        Fx.p(w, Particle.HAPPY_VILLAGER, s.at(0, 1, 0), 300, d("radius") / 2, 0);
        s.sound(s.at(), "item.bone_meal.use", 10f, 0.6f);
    }
}
