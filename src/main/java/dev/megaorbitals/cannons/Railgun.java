package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;
import java.util.*;

/** Fires a hypersonic slug from you to the target, ripping through everything in its path. */
public final class Railgun extends Cannon {
    public Railgun() {
        super("&b&lRailgun", Material.LIGHTNING_ROD, "A hypersonic slug from you to the target.", "Rips through everything in between.");
        warning(10, "#20E0FF");
        option("damage", 40.0, "Damage to everything along the line.");
        option("width", 2.0, "How close to the line you must be to get hit.");
        option("blast-every", 5, "Blocks between explosions along the line (0 = none).");
        option("blast-power", 3.0, "Power of those explosions.");
    }

    @Override public void warn(Strike s) { s.sound(s.at(), "block.beacon.power_select", 4f, 2f); }

    @Override
    public void strike(Strike s) {
        Location from = s.shooter != null ? s.shooter.getEyeLocation() : s.at(0, 60, 0);
        Location to = s.at();
        Vector dir = Fx.dir(from, to);
        double len = from.distance(to);
        Fx.line(s.world, Particle.END_ROD, from, to, 0.4);
        Fx.line(s.world, Particle.ELECTRIC_SPARK, from, to, 0.3);
        s.sound(from, "item.trident.thunder", 4f, 2f);
        s.sound(to, "entity.generic.explode", 6f, 1.5f);
        double w = d("width");
        Set<UUID> hit = new HashSet<>();
        int every = i("blast-every");
        for (double t = 2; t < len; t += 1) {
            Location p = from.clone().add(dir.clone().multiply(t));
            for (LivingEntity le : s.victims(p, w)) if (hit.add(le.getUniqueId())) s.damage(le, d("damage"));
            if (every > 0 && ((int) t) % every == 0) s.explode(p, (float) d("blast-power"), false);
        }
        s.explode(to, 6f, false);
    }
}
