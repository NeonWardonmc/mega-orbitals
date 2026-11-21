package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/** The Midas touch: the land turns to gold, and mobs caught in it are turned to gold statues. */
public final class Midas extends Cannon {
    public Midas() {
        super("&6&lMidas Touch", Material.GOLD_BLOCK, "The ground turns to gold, and anything", "caught in it is frozen as a gold statue.");
        warning(30, "#FFD700");
        option("radius", 20, "Range.");
        option("statue-mobs", true, "Turn mobs into gold block statues.");
        option("slow-seconds", 10, "Players are slowed (heavy with gold) for this long.");
    }

    @Override
    public void strike(Strike s) {
        int r = i("radius");
        s.blocks(Blocks.throttle(Blocks.surface(s.at(), r, Material.GOLD_BLOCK), 300));
        for (LivingEntity le : s.victims(s.at(), r)) {
            if (le instanceof Player p) {
                p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, i("slow-seconds") * 20, 3));
                p.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, i("slow-seconds") * 20, 2));
                Fx.title(p, "&6&lGOLDEN", "&7Heavy, isn't it?", 30);
            } else if (b("statue-mobs") && s.breaks()) {
                Location l = le.getLocation();
                double h = le.getHeight();
                le.remove();
                for (int y = 0; y < Math.max(1, Math.round(h)); y++) Blocks.set(l.clone().add(0, y, 0).getBlock(), Material.GOLD_BLOCK);
            }
        }
        Fx.p(s.world, Particle.WAX_ON, s.at(0, 1, 0), 200, r / 2.0, 0.1);
        s.sound(s.at(), "block.amethyst_block.chime", 10f, 0.6f);
    }
}
