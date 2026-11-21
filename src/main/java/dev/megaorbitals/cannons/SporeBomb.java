package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/** A spore bomb: giant mushrooms burst out of mycelium and the air is thick with toxic spores. */
public final class SporeBomb extends Cannon {
    public SporeBomb() {
        super("&5&lSpore Bomb", Material.RED_MUSHROOM, "Giant mushrooms burst out of the ground", "and the air fills with toxic spores.");
        warning(30, "#A04080");
        option("radius", 20, "Range.");
        option("mushrooms", 15, "Giant mushrooms to grow.");
        option("spore-seconds", 20, "How long the spores linger.");
    }

    @Override
    public void strike(Strike s) {
        int r = i("radius");
        if (s.breaks()) {
            s.blocks(Blocks.surface(s.at(), r, Material.MYCELIUM));
            s.every(2, i("mushrooms"), t -> {
                Location l = s.randomPoint(r);
                s.world.generateTree(l, Math.random() < 0.5 ? TreeType.RED_MUSHROOM : TreeType.BROWN_MUSHROOM);
            });
        }
        s.every(10, i("spore-seconds") * 2, t -> {
            Fx.p(s.world, Particle.SPORE_BLOSSOM_AIR, s.at(0, 3, 0), 80, r / 2.0, 0);
            Fx.p(s.world, Particle.CRIMSON_SPORE, s.at(0, 2, 0), 60, r / 2.0, 0);
            if (t % 2 == 0) {
                for (LivingEntity le : s.victims(s.at(), r)) {
                    le.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 200, 0));
                    le.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 40, 1));
                }
            }
        });
        s.sound(s.at(), "block.fungus.place", 10f, 0.4f);
    }
}
