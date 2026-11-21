package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.*;

/** Triple gravity: everyone nearby is so heavy they can barely jump and every fall hurts. */
public final class Heavyweight extends Cannon {
    public Heavyweight() {
        super("&8&lHeavyweight", Material.HEAVY_CORE, "Triple gravity: everyone nearby can barely", "jump, and every fall hurts a lot more.");
        warning(10, "#404040");
        noBlockDamage();
        option("radius", 25, "Range.");
        option("seconds", 60, "How long it lasts.");
        option("gravity", 3.0, "Gravity multiplier.");
    }

    @Override
    public void strike(Strike s) {
        long ticks = i("seconds") * 20L;
        for (LivingEntity le : s.victims(s.at(), d("radius"))) {
            s.tempAttribute(le, Attribute.GRAVITY, d("gravity") - 1, AttributeModifier.Operation.ADD_SCALAR, ticks);
            s.tempAttribute(le, Attribute.JUMP_STRENGTH, -0.6, AttributeModifier.Operation.ADD_SCALAR, ticks);
            s.tempAttribute(le, Attribute.SAFE_FALL_DISTANCE, -2, AttributeModifier.Operation.ADD_NUMBER, ticks);
            if (le instanceof Player p) p.setGliding(false);
            Fx.block(s.world, Particle.FALLING_DUST, le.getLocation().add(0, 2, 0), Material.ANVIL, 10, 0.3);
        }
        s.sound(s.at(), "block.anvil.land", 6f, 0.5f);
    }
}
