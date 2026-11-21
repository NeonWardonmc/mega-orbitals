package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.*;

/** Every mob nearby grows to three times its size, with triple health to match. */
public final class GiantMaker extends Cannon {
    public GiantMaker() {
        super("&2&lGigantify", Material.BONE_BLOCK, "Every mob nearby grows to three times", "its size, with triple health to match.");
        warning(20, "#40A040");
        noBlockDamage();
        option("radius", 25, "Range.");
        option("size", 3.0, "New size multiplier.");
        option("health", 3.0, "Health multiplier.");
        option("minutes", 10, "How long they stay giant.");
    }

    @Override
    public void strike(Strike s) {
        long ticks = i("minutes") * 1200L;
        for (LivingEntity le : s.victims(s.at(), d("radius"))) {
            if (le instanceof Player) continue;
            s.tempAttribute(le, Attribute.SCALE, d("size") - 1, AttributeModifier.Operation.ADD_SCALAR, ticks);
            s.tempAttribute(le, Attribute.MAX_HEALTH, d("health") - 1, AttributeModifier.Operation.ADD_SCALAR, ticks);
            s.tempAttribute(le, Attribute.ATTACK_DAMAGE, 1.0, AttributeModifier.Operation.ADD_SCALAR, ticks);
            var hp = le.getAttribute(Attribute.MAX_HEALTH);
            if (hp != null) le.setHealth(hp.getValue());
            s.hunt(le, 40);
            Fx.p(s.world, Particle.HAPPY_VILLAGER, le.getLocation().add(0, 1, 0), 20, 0.6, 0);
        }
        s.sound(s.at(), "entity.ravager.roar", 8f, 0.5f);
    }
}
