package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/** A thick smoke cloud. Everyone inside is blinded, except you, who becomes invisible in it. */
public final class SmokeScreen extends Cannon {
    public SmokeScreen() {
        super("&7&lSmoke Screen", Material.GRAY_DYE, "A thick smoke cloud. Everyone inside is blind,", "except you: you turn invisible in it.");
        warning(5, "#808080");
        noBlockDamage();
        option("radius", 15, "Size of the cloud.");
        option("seconds", 30, "How long the smoke lasts.");
    }

    @Override
    public void strike(Strike s) {
        double r = d("radius");
        s.every(5, i("seconds") * 4, t -> {
            Fx.p(s.world, Particle.CAMPFIRE_COSY_SMOKE, s.at(0, 2, 0), 60, r / 2, 0.01);
            for (LivingEntity le : s.victims(s.at(), r)) le.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 30, 0, false, false));
            if (s.shooter != null && s.shooter.getWorld() == s.world && s.shooter.getLocation().distance(s.at()) <= r) {
                s.shooter.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 30, 0, false, false));
            }
        });
        s.sound(s.at(), "block.fire.extinguish", 8f, 0.5f);
    }
}
