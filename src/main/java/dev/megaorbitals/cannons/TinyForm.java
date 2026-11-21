package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.*;

/** YOU shrink to the size of a mouse: tiny, fast and very hard to hit. */
public final class TinyForm extends Cannon {
    public TinyForm() {
        super("&a&lMouse Form", Material.RABBIT_FOOT, "YOU shrink to the size of a mouse:", "tiny, fast and very hard to hit.");
        warning(5, "#80FF80");
        noBlockDamage();
        option("seconds", 60, "How long you stay tiny.");
        option("size", 0.25, "Your size (1.0 = normal).");
        option("speed-bonus", 0.6, "Extra speed (0.6 = +60%).");
    }

    @Override
    public void strike(Strike s) {
        Player me = s.shooter;
        if (me == null) return;
        long ticks = i("seconds") * 20L;
        s.tempAttribute(me, Attribute.SCALE, d("size") - 1, AttributeModifier.Operation.ADD_SCALAR, ticks);
        s.tempAttribute(me, Attribute.MOVEMENT_SPEED, d("speed-bonus"), AttributeModifier.Operation.ADD_SCALAR, ticks);
        s.tempAttribute(me, Attribute.SAFE_FALL_DISTANCE, 20, AttributeModifier.Operation.ADD_NUMBER, ticks);
        Fx.title(me, "&a&lSQUEAK", "&7You're tiny for " + i("seconds") + " seconds", 30);
        Fx.p(me.getWorld(), Particle.CLOUD, me.getLocation(), 20, 0.3, 0.05);
        me.playSound(me.getLocation(), "entity.rabbit.ambient", 1f, 2f);
    }
}
