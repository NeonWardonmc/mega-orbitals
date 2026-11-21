package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import java.util.*;

/** Security lasers sweep back and forth across the area at waist height. Jump or get cut. */
public final class LaserGrid extends Cannon {
    public LaserGrid() {
        super("&c&lLaser Grid", Material.REDSTONE_TORCH, "Security lasers sweep back and forth across", "the area at waist height. Jump or get cut.");
        warning(20, "#FF2020");
        noBlockDamage();
        option("radius", 20, "Half the size of the grid.");
        option("seconds", 20, "How long the grid runs.");
        option("lasers", 3, "Lasers per direction.");
        option("damage", 6.0, "Damage per hit.");
    }

    @Override
    public void strike(Strike s) {
        double r = d("radius");
        int n = i("lasers");
        Color red = Color.fromRGB(255, 20, 20);
        Map<UUID, Integer> lastHit = new HashMap<>();
        s.every(2, i("seconds") * 10, t -> {
            for (int k = 0; k < n; k++) {
                double phase = t * 0.05 + k * (2 * Math.PI / n);
                double off = Math.sin(phase) * r;
                boolean alongX = k % 2 == 0;
                Location a = alongX ? s.at(-r, 1, off) : s.at(off, 1, -r);
                Location b = alongX ? s.at(r, 1, off) : s.at(off, 1, r);
                Fx.dustLine(s.world, a, b, red, 1f, 0.8);
                for (LivingEntity le : s.victims(s.at(), r * 1.5)) {
                    Location l = le.getLocation();
                    double across = alongX ? Math.abs(l.getZ() - a.getZ()) : Math.abs(l.getX() - a.getX());
                    double h = l.getY() - a.getY();
                    if (across > 0.8 || h > 0.6 || h < -1.5) continue;
                    if (t - lastHit.getOrDefault(le.getUniqueId(), -10) < 5) continue;
                    lastHit.put(le.getUniqueId(), t);
                    s.damage(le, d("damage"));
                    s.sound(l, "entity.blaze.hurt", 1f, 2f);
                }
            }
        });
    }
}
