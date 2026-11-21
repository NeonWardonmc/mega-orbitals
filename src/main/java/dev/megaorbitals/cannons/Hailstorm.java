package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.*;

/** Fist-sized chunks of ice hammer down, bruising and slowing everyone caught outside. */
public final class Hailstorm extends Cannon {
    public Hailstorm() {
        super("&b&lHailstorm", Material.PACKED_ICE, "Chunks of ice hammer down from the sky,", "bruising and slowing everyone outside.");
        warning(30, "#A0E0FF");
        option("radius", 22, "Area covered.");
        option("seconds", 12, "How long the hail lasts.");
        option("hail-per-tick", 5, "Ice chunks per tick.");
        option("damage-per-block-fallen", 0.6, "Damage per block the hail falls.");
    }

    @Override
    public void strike(Strike s) {
        Random rnd = new Random();
        double r = d("radius");
        s.every(1, i("seconds") * 20, t -> {
            for (int k = 0; k < i("hail-per-tick"); k++) {
                FallingBlock fb = s.fallingBlock(s.at(rnd.nextGaussian() * r / 2, 28, rnd.nextGaussian() * r / 2), Material.PACKED_ICE, true);
                fb.setHurtEntities(true);
                fb.setDamagePerBlock((float) d("damage-per-block-fallen"));
                fb.setMaxDamage(12);
            }
            if (t % 20 == 0) {
                for (LivingEntity le : s.victims(s.at(), r)) le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 30, 1));
                s.sound(s.at(), "block.glass.break", 4f, 1.6f);
            }
            Fx.p(s.world, Particle.SNOWFLAKE, s.at(0, 15, 0), 20, r / 2, 0.05);
        });
    }
}
