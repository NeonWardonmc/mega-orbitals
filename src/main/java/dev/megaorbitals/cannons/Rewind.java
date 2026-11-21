package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import java.util.*;

/** Marks everyone nearby. After a while, they snap back to where they were, at the health they had. */
public final class Rewind extends Cannon {
    public Rewind() {
        super("&e&lRewind", Material.RECOVERY_COMPASS, "Marks everyone nearby. Later, they snap back", "to where they were, with the health they had.");
        warning(10, "#FFE080");
        noBlockDamage();
        option("radius", 25, "Range.");
        option("seconds", 15, "Seconds before everyone is rewound.");
    }

    @Override
    public void strike(Strike s) {
        Map<LivingEntity, Location> where = new HashMap<>();
        Map<LivingEntity, Double> hp = new HashMap<>();
        for (LivingEntity le : s.victims(s.at(), d("radius"))) {
            where.put(le, le.getLocation());
            hp.put(le, le.getHealth());
            if (le instanceof Player p) s.actionBar(p, "&e⟲ You've been marked. Rewinding in " + i("seconds") + "s.");
        }
        s.every(20, i("seconds"), t -> where.values().forEach(l -> Fx.p(s.world, Particle.END_ROD, l.clone().add(0, 1, 0), 5, 0.3, 0)));
        s.later(i("seconds") * 20L, () -> where.forEach((le, l) -> {
            if (!le.isValid()) return;
            Fx.line(s.world, Particle.END_ROD, le.getLocation().add(0, 1, 0), l.clone().add(0, 1, 0), 0.5);
            le.teleport(l);
            double max = le.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH) == null ? 20 : le.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue();
            le.setHealth(Math.min(max, hp.get(le)));
            le.getWorld().playSound(l, "block.respawn_anchor.deplete", 2f, 1.5f);
        }));
    }
}
