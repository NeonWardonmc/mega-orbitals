package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import java.util.*;

/** The floor is scorching: anyone who stands still burns. Keep moving or cook. */
public final class HotFloor extends Cannon {
    public HotFloor() {
        super("&6&lHot Floor", Material.MAGMA_BLOCK, "The floor is scorching: anyone who stands", "still burns. Keep moving or cook.");
        warning(10, "#FF6020");
        noBlockDamage();
        option("radius", 20, "Range.");
        option("seconds", 30, "How long the floor stays hot.");
        option("damage", 3.0, "Damage per second for standing still.");
    }

    @Override
    public void strike(Strike s) {
        Map<UUID, Location> last = new HashMap<>();
        s.every(20, i("seconds"), t -> {
            Fx.p(s.world, Particle.FLAME, s.at(0, 0.2, 0), 80, d("radius") / 2, 0.01);
            for (LivingEntity le : s.victims(s.at(), d("radius"))) {
                Location prev = last.put(le.getUniqueId(), le.getLocation());
                if (prev == null || prev.getWorld() != le.getWorld()) continue;
                if (prev.distanceSquared(le.getLocation()) < 1.0) {
                    s.damage(le, d("damage"));
                    le.setFireTicks(30);
                    if (le instanceof Player p) s.actionBar(p, "&6&lMOVE! &7The floor is burning.");
                }
            }
        });
    }
}
