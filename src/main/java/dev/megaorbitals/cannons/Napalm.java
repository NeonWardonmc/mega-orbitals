package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;

/** A spreading ring of sticky fire that rolls outward and won't stop burning you. */
public final class Napalm extends Cannon {
    public Napalm() {
        super("&c&lNapalm", Material.FIRE_CHARGE, "A ring of sticky fire rolls outward.", "Once it's on you, you burn for a long time.");
        warning(25, "#FF4000");
        option("radius", 28, "How far the fire spreads.");
        option("damage", 4.0, "Damage when the fire wave hits you.");
        option("burn-seconds", 25, "How long you burn.");
    }

    @Override
    public void strike(Strike s) {
        Location c = s.at();
        s.explode(c, 3f, true);
        s.every(2, i("radius"), t -> {
            double r = t + 1;
            for (Location l : Fx.circle(c, r, (int) (r * 4))) {
                Location g = s.surface(l);
                Fx.p(s.world, Particle.FLAME, g, 2, 0.3, 0.02);
                Fx.p(s.world, Particle.LAVA, g, 1, 0.3, 0);
                Block b = g.getBlock();
                if (s.breaks() && b.getType().isAir()) Blocks.put(b, Material.FIRE, true);
            }
            for (LivingEntity le : s.victims(c, r + 1.5)) {
                if (le.getLocation().distance(c) < r - 1.5) continue;
                s.damage(le, d("damage"));
                le.setFireTicks(i("burn-seconds") * 20);
            }
            if (t % 4 == 0) s.sound(c, "item.firecharge.use", 6f, 0.5f);
        });
    }
}
