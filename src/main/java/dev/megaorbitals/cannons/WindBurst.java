package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;
import java.util.*;

/** A barrage of wind charges blasts everyone around like ragdolls. */
public final class WindBurst extends Cannon {
    public WindBurst() {
        super("&f&lHurricane Barrage", Material.WIND_CHARGE, "A barrage of wind charges blasts", "everyone around like ragdolls.");
        warning(20, "#E0F0FF");
        noBlockDamage();
        option("radius", 20, "Area covered.");
        option("seconds", 6, "Length of the barrage.");
        option("charges-per-tick", 2, "Wind charges per tick.");
    }

    @Override
    public void strike(Strike s) {
        Random rnd = new Random();
        s.every(1, i("seconds") * 20, t -> {
            List<LivingEntity> targets = s.victims(s.at(), d("radius"));
            for (int k = 0; k < i("charges-per-tick"); k++) {
                Location aim = !targets.isEmpty() && rnd.nextBoolean()
                        ? targets.get(rnd.nextInt(targets.size())).getLocation()
                        : s.randomPoint(d("radius"));
                Location from = aim.clone().add(rnd.nextGaussian() * 4, 20, rnd.nextGaussian() * 4);
                Vector v = Fx.dir(from, aim).multiply(2.0);
                s.world.spawn(from, WindCharge.class, w -> {
                    w.setVelocity(v);
                    w.setAcceleration(v.clone().multiply(0.1));
                    if (s.shooter != null) w.setShooter(s.shooter);
                    s.tag(w);
                });
            }
        });
    }
}
