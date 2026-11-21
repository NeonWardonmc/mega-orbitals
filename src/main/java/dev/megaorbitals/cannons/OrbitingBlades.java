package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import java.util.*;

/** Giant swords orbit the target in a widening ring, slicing anything that comes near. */
public final class OrbitingBlades extends Cannon {
    public OrbitingBlades() {
        super("&b&lBlade Orbit", Material.NETHERITE_SWORD, "Giant swords orbit the target in a widening", "ring, slicing anything that comes near.");
        warning(20, "#60A0C0");
        noBlockDamage();
        option("blades", 6, "Number of swords.");
        option("start-radius", 4.0, "Starting orbit size.");
        option("end-radius", 14.0, "Final orbit size.");
        option("seconds", 20, "How long they orbit.");
        option("damage", 10.0, "Damage per hit.");
    }

    @Override
    public void strike(Strike s) {
        int n = i("blades"), ticks = i("seconds") * 20;
        List<ItemDisplay> blades = new ArrayList<>();
        for (int k = 0; k < n; k++) blades.add(Fx.itemDisplay(s.at(0, 1.5, 0), Material.NETHERITE_SWORD, 3f));
        Map<UUID, Integer> lastHit = new HashMap<>();
        s.every(1, ticks, t -> {
            double r = d("start-radius") + (d("end-radius") - d("start-radius")) * t / ticks;
            for (int k = 0; k < n; k++) {
                double a = t * 0.15 + 2 * Math.PI * k / n;
                Location p = s.at(Math.cos(a) * r, 1.5, Math.sin(a) * r);
                p.setYaw((float) Math.toDegrees(a));
                blades.get(k).teleport(p);
                Fx.p(s.world, Particle.CRIT, p, 1, 0.1, 0);
                for (LivingEntity le : s.victims(p, 1.8)) {
                    if (t - lastHit.getOrDefault(le.getUniqueId(), -100) < 10) continue;
                    lastHit.put(le.getUniqueId(), t);
                    s.damage(le, d("damage"));
                    le.setVelocity(Fx.dir(s.at(), le.getLocation()).multiply(1.2).setY(0.4));
                }
            }
            if (t % 10 == 0) s.sound(s.at(), "entity.player.attack.sweep", 4f, 0.6f);
            if (t == ticks - 1) blades.forEach(Entity::remove);
        });
    }
}
