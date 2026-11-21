package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;

/** A guillotine falls on every target. Anyone below a health threshold is executed instantly. */
public final class Executioner extends Cannon {
    public Executioner() {
        super("&4&lExecutioner", Material.IRON_AXE, "A guillotine falls on every target. Anyone below", "40% health is executed instantly.");
        warning(25, "#800000");
        noBlockDamage();
        option("radius", 25, "Range.");
        option("threshold", 0.4, "Health fraction below which targets are executed.");
        option("damage", 6.0, "Damage to everyone else.");
    }

    @Override
    public void strike(Strike s) {
        for (LivingEntity le : s.victims(s.at(), d("radius"))) {
            Location top = le.getLocation().add(0, 12, 0);
            BlockDisplay blade = Fx.display(top, Material.IRON_BLOCK, 1.6f, 0.3f, 1.6f);
            s.every(1, 9, t -> {
                blade.teleport(top.clone().subtract(0, (t + 1) * 1.2, 0));
                if (t != 8) return;
                blade.remove();
                var max = le.getAttribute(Attribute.MAX_HEALTH);
                double frac = le.getHealth() / (max == null ? 20 : max.getValue());
                if (frac <= d("threshold")) {
                    s.damage(le, 10000);
                    Fx.p(s.world, Particle.DAMAGE_INDICATOR, le.getLocation().add(0, 1, 0), 20, 0.3, 0.1);
                    s.sound(le.getLocation(), "entity.wither.break_block", 3f, 0.6f);
                } else {
                    s.damage(le, d("damage"));
                    s.sound(le.getLocation(), "block.anvil.land", 2f, 1.2f);
                }
            });
        }
    }
}
