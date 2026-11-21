package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;

/** A coven of illusioners: they blind you, split into clones and rain arrows. */
public final class IllusionerCoven extends Cannon {
    public IllusionerCoven() {
        super("&9&lIllusioner Coven", Material.BOW, "A coven of illusioners: they blind you,", "split into clones and rain arrows.");
        warning(25, "#4060C0");
        option("illusioners", 5, "Number of illusioners.");
        option("radius", 8.0, "Spread.");
    }

    @Override
    public void strike(Strike s) {
        for (Location l : Fx.circle(s.at(), d("radius"), i("illusioners"))) {
            Illusioner il = s.mob(s.surface(l), Illusioner.class, null);
            s.hunt(il, 50);
            Fx.p(s.world, Particle.WITCH, il.getLocation().add(0, 1, 0), 30, 0.4, 0.05);
        }
        s.sound(s.at(), "entity.illusioner.prepare_mirror", 10f, 0.8f);
    }
}
