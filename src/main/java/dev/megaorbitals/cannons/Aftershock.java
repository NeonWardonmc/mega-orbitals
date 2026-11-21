package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;

/** One massive blast, then aftershock after aftershock rocks the same spot, shaking everyone. */
public final class Aftershock extends Cannon {
    public Aftershock() {
        super("&6&lAftershock", Material.CRACKED_DEEPSLATE_BRICKS, "One massive blast, then aftershock after", "aftershock rocks the same spot.");
        warning(25, "#C08030");
        option("power", 8.0, "Power of the main blast.");
        option("aftershocks", 6, "Number of aftershocks.");
        option("shake-radius", 30, "How far the shaking is felt.");
    }

    @Override
    public void strike(Strike s) {
        s.explode(s.at(), (float) d("power"), false);
        int n = i("aftershocks");
        for (int k = 1; k <= n; k++) {
            float p = (float) (d("power") * (1 - (double) k / (n + 1)));
            s.later(k * 25L, () -> {
                s.explode(s.at(), p, false);
                for (LivingEntity le : s.victims(s.at(), d("shake-radius"))) {
                    le.setVelocity(le.getVelocity().add(new Vector(Math.random() - 0.5, 0.4, Math.random() - 0.5)));
                }
                Fx.ring(s.world, Particle.EXPLOSION, s.at(0, 0.5, 0), p * 2, 16);
            });
        }
    }
}
