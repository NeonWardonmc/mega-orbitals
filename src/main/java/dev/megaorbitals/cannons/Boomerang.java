package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;
import java.util.*;

/** Spinning giant axes fly out from the target and come back, cutting everything twice. */
public final class Boomerang extends Cannon {
    public Boomerang() {
        super("&7&lBoomerang Axes", Material.IRON_AXE, "Spinning giant axes fly out from the", "target and come back, cutting everything twice.");
        warning(20, "#B0B0B0");
        noBlockDamage();
        option("axes", 4, "Number of axes.");
        option("range", 25, "How far they fly out.");
        option("damage", 9.0, "Damage per hit.");
    }

    @Override
    public void strike(Strike s) {
        int n = i("axes"), range = i("range");
        for (int k = 0; k < n; k++) {
            double a = 2 * Math.PI * k / n;
            ItemDisplay axe = Fx.itemDisplay(s.at(0, 1.5, 0), Material.IRON_AXE, 3f);
            Map<UUID, Integer> lastHit = new HashMap<>();
            int total = range * 4;
            s.every(1, total, t -> {
                double out = t < total / 2 ? t / 2.0 : (total - t) / 2.0;
                Location p = s.at(Math.cos(a) * out, 1.5, Math.sin(a) * out);
                axe.teleport(p);
                axe.setTransformation(new Transformation(new Vector3f(), new AxisAngle4f((float) (t * 0.6), 0, 1, 0),
                        new Vector3f(3, 3, 3), new AxisAngle4f()));
                for (LivingEntity le : s.victims(p, 2)) {
                    if (t - lastHit.getOrDefault(le.getUniqueId(), -100) < 15) continue;
                    lastHit.put(le.getUniqueId(), t);
                    s.damage(le, d("damage"));
                    s.sound(p, "entity.player.attack.sweep", 2f, 0.8f);
                }
                if (t % 6 == 0) s.sound(p, "item.trident.riptide_1", 1f, 1.6f);
                if (t == total - 1) axe.remove();
            });
        }
    }
}
