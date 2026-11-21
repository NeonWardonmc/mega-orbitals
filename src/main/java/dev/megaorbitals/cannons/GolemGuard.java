package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;

/** Drops iron golem bodyguards that defend the area and attack anyone hostile. */
public final class GolemGuard extends Cannon {
    public GolemGuard() {
        super("&f&lGolem Guard", Material.IRON_BLOCK, "Drops iron golem bodyguards.", "They attack everyone except you.");
        warning(20, "#E0E0E0");
        noBlockDamage();
        option("golems", 4, "Number of golems.");
        option("hunt-radius", 30, "How far they look for targets.");
    }

    @Override
    public void strike(Strike s) {
        for (Location l : Fx.circle(s.at(0, 20, 0), 3, i("golems"))) {
            IronGolem g = s.mob(l, IronGolem.class, go -> go.setPlayerCreated(true));
            s.later(40, () -> {
                LivingEntity t = s.nearest(g.getLocation(), d("hunt-radius"));
                if (t != null && !(t instanceof IronGolem)) g.setTarget(t);
            });
        }
        s.sound(s.at(), "entity.iron_golem.repair", 10f, 0.5f);
    }
}
