package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.util.Vector;
import java.util.Random;

/** Bombs that bounce around, exploding every time they hit the ground. */
public final class BouncingBetty extends Cannon {
    public BouncingBetty() {
        super("&e&lBouncing Betty", Material.SLIME_BALL, "Bombs that bounce around,", "exploding on every bounce.");
        warning(20, "#FFE020");
        option("bombs", 8, "Number of bouncing bombs.");
        option("bounces", 5, "Bounces before the final blast.");
        option("bounce-power", 2.5, "Explosion power of each bounce.");
        option("final-power", 5.0, "Explosion power of the final blast.");
    }

    @Override
    public void strike(Strike s) {
        Random rnd = new Random();
        int max = i("bounces");
        for (int k = 0; k < i("bombs"); k++) {
            TNTPrimed t = s.tnt(s.at(rnd.nextGaussian() * 4, 25, rnd.nextGaussian() * 4), 0f, 2000);
            int[] bounces = {0};
            s.loop(1, tick -> {
                if (!t.isValid() || tick > 900) { t.remove(); return false; }
                Fx.p(s.world, Particle.FLAME, t.getLocation(), 1, 0.1, 0);
                if (!t.isOnGround()) return true;
                Location l = t.getLocation();
                if (++bounces[0] > max) {
                    t.remove();
                    s.explode(l, (float) d("final-power"), false);
                    return false;
                }
                s.explode(l, (float) d("bounce-power"), false);
                t.setVelocity(new Vector(rnd.nextDouble(-0.6, 0.6), 1.0 + rnd.nextDouble(0.4), rnd.nextDouble(-0.6, 0.6)));
                return true;
            });
        }
    }
}
