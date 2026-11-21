package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import java.util.*;

/** A bolt that jumps from target to target, chaining through everyone standing close together. */
public final class ChainLightning extends Cannon {
    public ChainLightning() {
        super("&b&lChain Lightning", Material.COPPER_INGOT, "A bolt that jumps from target to target.", "Don't stand close together.");
        warning(20, "#60C0FF");
        option("jumps", 25, "Maximum jumps.");
        option("jump-range", 14.0, "How far it can jump.");
        option("damage", 12.0, "Damage per hit.");
        option("ticks-per-jump", 3, "Delay between jumps.");
    }

    @Override
    public void strike(Strike s) {
        Location[] from = {s.at(0, 1, 0)};
        Set<UUID> hit = new HashSet<>();
        s.world.strikeLightningEffect(s.at());
        s.loop(i("ticks-per-jump"), t -> {
            if (t >= i("jumps")) return false;
            LivingEntity next = null;
            double best = Double.MAX_VALUE;
            for (LivingEntity le : s.victims(from[0], d("jump-range"))) {
                double dd = le.getLocation().distanceSquared(from[0]);
                if (!hit.contains(le.getUniqueId()) && dd < best) { best = dd; next = le; }
            }
            if (next == null) return false;
            hit.add(next.getUniqueId());
            Location to = next.getLocation().add(0, 1, 0);
            Fx.line(s.world, Particle.ELECTRIC_SPARK, from[0], to, 0.3);
            Fx.dustLine(s.world, from[0], to, Color.fromRGB(150, 220, 255), 1.2f, 0.4);
            s.world.strikeLightningEffect(next.getLocation());
            s.damage(next, d("damage"));
            from[0] = to;
            return true;
        });
    }
}
