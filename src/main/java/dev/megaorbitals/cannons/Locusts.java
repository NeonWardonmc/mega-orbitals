package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.*;

/** A plague of locusts strips the land bare: crops, grass, flowers and leaves are devoured. */
public final class Locusts extends Cannon {
    public Locusts() {
        super("&2&lLocust Plague", Material.WHEAT, "A plague of locusts strips the land bare:", "crops, grass, flowers and leaves, devoured.");
        warning(15, "#607020");
        option("radius", 25, "Range.");
        option("seconds", 25, "How long the swarm feeds.");
        option("eaten-per-second", 60, "Plants eaten each second.");
    }

    private static boolean edible(Material m) {
        String n = m.name();
        return n.contains("LEAVES") || n.contains("FLOWER") || n.contains("TULIP") || n.equals("SHORT_GRASS") || n.equals("TALL_GRASS")
                || n.equals("FERN") || n.equals("LARGE_FERN") || n.equals("WHEAT") || n.equals("CARROTS") || n.equals("POTATOES")
                || n.equals("BEETROOTS") || n.equals("MELON") || n.equals("PUMPKIN") || n.equals("SUGAR_CANE") || n.equals("DANDELION")
                || n.equals("POPPY") || n.equals("OXEYE_DAISY") || n.equals("CORNFLOWER") || n.equals("AZURE_BLUET") || n.equals("BUSH");
    }

    @Override
    public void strike(Strike s) {
        Random rnd = new Random();
        double r = d("radius");
        s.every(10, i("seconds") * 2, t -> {
            Fx.dust(s.world, s.at(rnd.nextGaussian() * r / 3, 3, rnd.nextGaussian() * r / 3), Color.fromRGB(60, 50, 20), 0.8f, 120, r / 3);
            if (s.breaks()) {
                for (int k = 0; k < i("eaten-per-second") / 2; k++) {
                    Location p = s.randomPoint(r);
                    Block top = s.world.getHighestBlockAt(p);
                    for (int y = 0; y < 3; y++) {
                        Block b = top.getRelative(0, -y + 1, 0);
                        if (edible(b.getType())) Blocks.set(b, Material.AIR);
                        else if (b.getType() == Material.GRASS_BLOCK) Blocks.set(b, Material.DIRT);
                    }
                }
            }
            for (LivingEntity le : s.victims(s.at(), r)) {
                le.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 30, 1, false, false));
                if (t % 4 == 0) s.damage(le, 0.5);
            }
            if (t % 4 == 0) s.sound(s.at(), "entity.bee.loop_aggressive", 6f, 0.5f);
        });
    }
}
