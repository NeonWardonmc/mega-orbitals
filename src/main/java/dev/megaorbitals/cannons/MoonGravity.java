package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.*;

/** Moon gravity: everyone nearby floats around in huge, slow bouncy jumps. */
public final class MoonGravity extends Cannon {
    public MoonGravity() {
        super("&7&lMoon Gravity", Material.END_STONE, "Moon gravity: everyone nearby floats", "around in huge, slow, bouncy jumps.");
        warning(10, "#C0C0C0");
        noBlockDamage();
        option("radius", 25, "Range.");
        option("seconds", 60, "How long it lasts.");
        option("gravity", 0.17, "Gravity multiplier (1.0 = normal).");
    }

    @Override
    public void strike(Strike s) {
        long ticks = i("seconds") * 20L;
        for (LivingEntity le : s.victims(s.at(), d("radius"))) {
            s.tempAttribute(le, Attribute.GRAVITY, d("gravity") - 1, AttributeModifier.Operation.ADD_SCALAR, ticks);
            s.tempAttribute(le, Attribute.SAFE_FALL_DISTANCE, 30, AttributeModifier.Operation.ADD_NUMBER, ticks);
            s.tempAttribute(le, Attribute.JUMP_STRENGTH, 0.5, AttributeModifier.Operation.ADD_SCALAR, ticks);
            Fx.p(s.world, Particle.END_ROD, le.getLocation(), 10, 0.3, 0.02);
        }
        s.sound(s.at(), "block.beacon.activate", 6f, 1.5f);
    }
}
