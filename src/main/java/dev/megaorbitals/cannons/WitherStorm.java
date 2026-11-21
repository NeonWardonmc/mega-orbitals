package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;

/** Summons a squadron of withers above the target. */
public final class WitherStorm extends Cannon {
    public WitherStorm() {
        super("&8&lWither Squadron", Material.WITHER_SKELETON_SKULL, "Summons a squadron of withers", "hovering over the target.");
        warning(60, "#202020");
        option("withers", 3, "Number of withers.");
        option("health-multiplier", 1.0, "Multiply each wither's health.");
    }

    @Override
    public void strike(Strike s) {
        for (Location l : Fx.circle(s.at(0, 8, 0), 6, i("withers"))) {
            Wither w = s.mob(l, Wither.class, wi -> {
                var attr = wi.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH);
                if (attr != null) {
                    attr.setBaseValue(attr.getBaseValue() * d("health-multiplier"));
                    wi.setHealth(attr.getBaseValue());
                }
            });
            s.hunt(w, 80);
            Fx.p(s.world, Particle.SOUL, l, 60, 1.5, 0.05);
        }
        s.soundWorld("entity.wither.spawn", 1f, 0.8f);
    }
}
