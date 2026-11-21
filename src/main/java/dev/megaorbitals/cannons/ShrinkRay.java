package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.*;

/** Shrinks everyone nearby to a quarter of their size. Tiny, slow and weak. */
public final class ShrinkRay extends Cannon {
    public ShrinkRay() {
        super("&d&lShrink Ray", Material.POISONOUS_POTATO, "Shrinks everyone nearby to a quarter of", "their size. Tiny, slow and weak.");
        warning(15, "#FF80FF");
        noBlockDamage();
        option("radius", 20, "Range.");
        option("seconds", 60, "How long they stay small.");
        option("size", 0.25, "New size (1.0 = normal).");
    }

    @Override
    public void strike(Strike s) {
        long ticks = i("seconds") * 20L;
        for (LivingEntity le : s.victims(s.at(), d("radius"))) {
            s.tempAttribute(le, Attribute.SCALE, d("size") - 1, AttributeModifier.Operation.ADD_SCALAR, ticks);
            s.tempAttribute(le, Attribute.ATTACK_DAMAGE, -0.5, AttributeModifier.Operation.ADD_SCALAR, ticks);
            Fx.p(s.world, Particle.WITCH, le.getLocation().add(0, 0.5, 0), 20, 0.3, 0.05);
            if (le instanceof Player p) Fx.title(p, "&d&lSHRUNK", "&7for " + i("seconds") + " seconds", 30);
        }
        s.sound(s.at(), "entity.illusioner.mirror_move", 8f, 2f);
    }
}
