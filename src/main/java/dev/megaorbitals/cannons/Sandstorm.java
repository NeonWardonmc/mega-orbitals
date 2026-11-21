package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import java.util.*;

/** A blinding sandstorm: howling wind shoves everyone and buries the area in drifting sand. */
public final class Sandstorm extends Cannon {
    public Sandstorm() {
        super("&e&lSandstorm", Material.SANDSTONE, "A blinding sandstorm shoves everyone", "around and buries the area in sand.");
        warning(30, "#E0C070");
        option("radius", 30, "Storm radius.");
        option("seconds", 20, "Storm length.");
        option("wind", 0.25, "Wind strength.");
        option("sand-per-second", 15, "Sand piles dropped each second.");
    }

    @Override
    public void strike(Strike s) {
        Random rnd = new Random();
        double r = d("radius");
        Vector wind = new Vector(1, 0, 0).rotateAroundY(rnd.nextDouble() * Math.PI * 2);
        s.every(2, i("seconds") * 10, t -> {
            for (int k = 0; k < 40; k++) {
                Location l = s.at(rnd.nextGaussian() * r / 2, rnd.nextDouble() * 6, rnd.nextGaussian() * r / 2);
                Fx.block(s.world, Particle.FALLING_DUST, l, Material.SAND, 1, 0.3);
            }
            for (LivingEntity le : s.victims(s.at(), r)) {
                le.setVelocity(le.getVelocity().add(wind.clone().multiply(d("wind"))));
                le.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 30, 0, false, false));
            }
            if (t % 10 == 0) {
                wind.rotateAroundY(rnd.nextGaussian() * 0.3);
                s.sound(s.at(), "item.elytra.flying", 4f, 0.6f);
                if (s.breaks()) for (int k = 0; k < i("sand-per-second"); k++) s.fallingBlock(s.randomPoint(r).add(0, 8, 0), Material.SAND, false);
            }
        });
    }
}
