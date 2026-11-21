package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;

/** Anvils fall directly on top of every living thing in range, then keep tracking them. */
public final class AnvilRain extends Cannon {
    public AnvilRain() {
        super("&7&lAnvil Hunters", Material.ANVIL, "Anvils drop straight onto every target", "in range. Again. And again.");
        warning(20, "#909090");
        option("radius", 25, "Range.");
        option("volleys", 6, "Number of volleys.");
        option("drop-height", 15, "How high above each target the anvil drops from.");
        option("leave-anvils", false, "Leave the anvils behind.");
    }

    @Override
    public void strike(Strike s) {
        s.every(15, i("volleys"), t -> {
            for (LivingEntity le : s.victims(s.at(), d("radius"))) {
                FallingBlock fb = s.fallingBlock(le.getLocation().add(0, i("drop-height"), 0), Material.ANVIL, !b("leave-anvils"));
                fb.setHurtEntities(true);
                fb.setDamagePerBlock(2f);
                fb.setMaxDamage(40);
            }
            s.sound(s.at(), "block.anvil.place", 6f, 0.6f);
        });
    }
}
