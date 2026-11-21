package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;
import java.util.*;

/** Lightning hits the ground and splits into a branching tree of electricity racing outward. */
public final class FractalLightning extends Cannon {
    public FractalLightning() {
        super("&b&lFractal Lightning", Material.LIGHTNING_ROD, "Lightning hits the ground and splits into a", "branching tree of electricity racing outward.");
        warning(20, "#A0E0FF");
        option("branches", 5, "Starting branches.");
        option("depth", 4, "How many times branches split.");
        option("segment-length", 6.0, "Length of each branch segment.");
        option("damage", 8.0, "Damage near each branch.");
    }

    @Override
    public void strike(Strike s) {
        s.world.strikeLightningEffect(s.at());
        Random rnd = new Random();
        for (int k = 0; k < i("branches"); k++) {
            double a = 2 * Math.PI * k / i("branches") + rnd.nextGaussian() * 0.2;
            branch(s, s.at(0, 0.3, 0), new Vector(Math.cos(a), 0, Math.sin(a)), 0, new HashSet<>(), rnd);
        }
    }

    private void branch(Strike s, Location from, Vector dir, int level, Set<UUID> hit, Random rnd) {
        if (level > i("depth")) return;
        Location to = s.surface(from.clone().add(dir.clone().multiply(d("segment-length")))).add(0, 0.3, 0);
        s.later(level * 3L, () -> {
            Fx.line(s.world, Particle.ELECTRIC_SPARK, from, to, 0.25);
            Fx.dustLine(s.world, from, to, Color.fromRGB(180, 230, 255), 1f, 0.4);
            for (LivingEntity le : s.victims(to, 2.5)) if (hit.add(le.getUniqueId())) s.damage(le, d("damage"));
            if (level == i("depth")) s.world.strikeLightningEffect(to);
            s.sound(to, "entity.lightning_bolt.impact", 2f, 1.6f);
        });
        int kids = level < 2 ? 2 : 1 + rnd.nextInt(2);
        for (int k = 0; k < kids; k++) {
            Vector nd = dir.clone().rotateAroundY(rnd.nextGaussian() * 0.6);
            branch(s, to, nd, level + 1, hit, rnd);
        }
    }
}
