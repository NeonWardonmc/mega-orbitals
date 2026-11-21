package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;

/** Waves of evoker fangs ripple outward from the target in rings. */
public final class EvokerFangs extends Cannon {
    public EvokerFangs() {
        super("&5&lFang Ripple", Material.TOTEM_OF_UNDYING, "Waves of evoker fangs ripple", "outward from the target in rings.");
        warning(20, "#8040A0");
        noBlockDamage();
        option("radius", 24, "How far the rings travel.");
        option("waves", 3, "Number of waves.");
        option("ring-spacing", 1.5, "Blocks between rings.");
    }

    @Override
    public void strike(Strike s) {
        double sp = d("ring-spacing");
        int rings = (int) (d("radius") / sp);
        for (int w = 0; w < i("waves"); w++) {
            s.later(w * 30L, () -> s.every(2, rings, t -> {
                double r = (t + 1) * sp;
                for (Location l : Fx.circle(s.at(), r, (int) Math.max(6, r * 4))) {
                    s.world.spawn(s.surface(l), org.bukkit.entity.EvokerFangs.class, f -> { if (s.shooter != null) f.setOwner(s.shooter); });
                }
            }));
        }
        s.sound(s.at(), "entity.evoker.prepare_attack", 10f, 0.8f);
    }
}
