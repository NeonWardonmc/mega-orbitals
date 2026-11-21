package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/** Thermobaric bomb: a cloud of fuel spreads out over the area, then the whole cloud ignites at once. */
public final class FuelAir extends Cannon {
    public FuelAir() {
        super("&6&lThermobaric Bomb", Material.FIRE_CHARGE, "A cloud of fuel spreads over the area,", "then the whole cloud ignites at once.");
        warning(20, "#FFA040");
        option("radius", 16, "Size of the fuel cloud.");
        option("spread-seconds", 5, "Time the cloud spreads before igniting.");
        option("blast-spacing", 4, "Blocks between blasts in the cloud.");
        option("power", 2.5, "Power of each blast.");
    }

    @Override
    public void strike(Strike s) {
        int r = i("radius");
        int spread = i("spread-seconds") * 20;
        s.every(4, spread / 4, t -> {
            double cr = r * (t + 1) / (spread / 4.0);
            Fx.p(s.world, Particle.CLOUD, s.at(0, 2, 0), (int) (cr * 6), cr / 2, 0.01);
            for (LivingEntity le : s.victims(s.at(), cr)) le.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 60, 0, false, false));
        });
        s.sound(s.at(), "block.fire.extinguish", 8f, 0.4f);
        s.later(spread, () -> {
            int step = Math.max(2, i("blast-spacing"));
            for (int x = -r; x <= r; x += step) for (int z = -r; z <= r; z += step) {
                if (x * x + z * z > r * r) continue;
                Location l = s.surface(s.at(x, 0, z)).add(0, 1, 0);
                s.later((long) (Math.sqrt(x * x + z * z) / 3), () -> s.explode(l, (float) d("power"), true));
            }
            s.soundWorld("entity.generic.explode", 1f, 0.3f);
        });
    }
}
