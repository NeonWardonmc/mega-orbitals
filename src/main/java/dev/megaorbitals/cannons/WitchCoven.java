package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;

/** A witch coven appears in a circle with their black cats, and starts brewing trouble. */
public final class WitchCoven extends Cannon {
    public WitchCoven() {
        super("&5&lWitch Coven", Material.CAULDRON, "A witch coven appears in a circle with their", "black cats, and starts brewing trouble.");
        warning(25, "#6020A0");
        option("witches", 6, "Number of witches.");
        option("cats", 4, "Black cats.");
        option("radius", 6.0, "Size of the circle.");
    }

    @Override
    public void strike(Strike s) {
        for (Location l : Fx.circle(s.at(), d("radius"), i("witches"))) {
            Witch w = s.mob(s.surface(l), Witch.class, null);
            s.hunt(w, 40);
            Fx.p(s.world, Particle.WITCH, w.getLocation().add(0, 1, 0), 30, 0.4, 0.05);
        }
        for (int k = 0; k < i("cats"); k++) {
            s.mob(s.randomPoint(d("radius")), Cat.class, c -> c.setCatType(Cat.Type.ALL_BLACK));
        }
        if (s.breaks()) s.at().getBlock().setType(Material.CAULDRON);
        s.every(10, 20, t -> Fx.p(s.world, Particle.WITCH, s.at(0, 1, 0), 10, 0.3, 0.1));
        s.sound(s.at(), "entity.witch.celebrate", 10f, 0.8f);
    }
}
