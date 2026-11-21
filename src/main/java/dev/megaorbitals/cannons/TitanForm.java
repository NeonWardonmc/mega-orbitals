package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.*;

/** YOU become a titan: giant size, double health, huge reach and a crushing punch. */
public final class TitanForm extends Cannon {
    public TitanForm() {
        super("&6&lTitan Form", Material.TOTEM_OF_UNDYING, "YOU become a titan: giant size, double health,", "huge reach and a crushing punch.");
        warning(10, "#FFC040");
        noBlockDamage();
        option("seconds", 60, "How long you stay a titan.");
        option("size", 3.0, "Your size multiplier.");
        option("bonus-health", 20.0, "Extra max health.");
        option("bonus-reach", 4.0, "Extra reach in blocks.");
    }

    @Override
    public void strike(Strike s) {
        Player me = s.shooter;
        if (me == null) return;
        long ticks = i("seconds") * 20L;
        s.tempAttribute(me, Attribute.SCALE, d("size") - 1, AttributeModifier.Operation.ADD_SCALAR, ticks);
        s.tempAttribute(me, Attribute.MAX_HEALTH, d("bonus-health"), AttributeModifier.Operation.ADD_NUMBER, ticks);
        s.tempAttribute(me, Attribute.ENTITY_INTERACTION_RANGE, d("bonus-reach"), AttributeModifier.Operation.ADD_NUMBER, ticks);
        s.tempAttribute(me, Attribute.BLOCK_INTERACTION_RANGE, d("bonus-reach"), AttributeModifier.Operation.ADD_NUMBER, ticks);
        s.tempAttribute(me, Attribute.ATTACK_DAMAGE, 1.0, AttributeModifier.Operation.ADD_SCALAR, ticks);
        s.tempAttribute(me, Attribute.STEP_HEIGHT, 1.5, AttributeModifier.Operation.ADD_NUMBER, ticks);
        s.tempAttribute(me, Attribute.SAFE_FALL_DISTANCE, 10, AttributeModifier.Operation.ADD_NUMBER, ticks);
        var hp = me.getAttribute(Attribute.MAX_HEALTH);
        s.later(2, () -> { if (hp != null) me.setHealth(hp.getValue()); });
        Fx.title(me, "&6&lTITAN", "&7for " + i("seconds") + " seconds", 30);
        me.getWorld().playSound(me.getLocation(), "entity.ravager.roar", 4f, 0.5f);
    }
}
