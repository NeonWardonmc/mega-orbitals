package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.*;

/** Reality glitches: blocks swap places at random and everyone flickers around. */
public final class Glitch extends Cannon {
    public Glitch() {
        super("&5&lReality Glitch", Material.STRUCTURE_VOID, "Reality glitches: blocks swap places", "at random and everyone flickers around.");
        warning(20, "#A000FF");
        option("radius", 12, "Range.");
        option("swaps", 1500, "Number of block swaps.");
        option("seconds", 8, "How long it glitches.");
    }

    @Override
    public void strike(Strike s) {
        Random rnd = new Random();
        int r = i("radius");
        int ticks = i("seconds") * 20;
        int perTick = Math.max(1, i("swaps") / ticks);
        s.every(1, ticks, t -> {
            if (s.breaks()) {
                for (int k = 0; k < perTick; k++) {
                    Block a = s.at(rnd.nextInt(2 * r + 1) - r, rnd.nextInt(r) - r / 2, rnd.nextInt(2 * r + 1) - r).getBlock();
                    Block b = s.at(rnd.nextInt(2 * r + 1) - r, rnd.nextInt(r) - r / 2, rnd.nextInt(2 * r + 1) - r).getBlock();
                    if (!Blocks.breakable(a) || !Blocks.breakable(b)) continue;
                    BlockData da = a.getBlockData(), db = b.getBlockData();
                    a.setBlockData(db, false);
                    b.setBlockData(da, false);
                }
            }
            if (t % 4 == 0) {
                Fx.dust(s.world, s.at(0, 2, 0), rnd.nextBoolean() ? Color.fromRGB(255, 0, 255) : Color.fromRGB(0, 255, 120), 1.5f, 30, r / 2.0);
                for (LivingEntity le : s.victims(s.at(), r)) {
                    if (rnd.nextInt(3) == 0) le.teleport(le.getLocation().add(rnd.nextInt(5) - 2, 0, rnd.nextInt(5) - 2));
                    le.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 60, 0, false, false));
                }
                s.sound(s.at(), "block.note_block.bit", 3f, 0.5f + rnd.nextFloat() * 1.5f);
            }
        });
    }
}
