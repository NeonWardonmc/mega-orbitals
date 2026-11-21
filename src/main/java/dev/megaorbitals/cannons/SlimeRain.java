package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;

/** Huge slimes and magma cubes rain down and split into hordes of smaller ones. */
public final class SlimeRain extends Cannon {
    public SlimeRain() {
        super("&a&lSlime Rain", Material.SLIME_BLOCK, "Huge slimes and magma cubes rain down", "and split into hordes of smaller ones.");
        warning(20, "#60E060");
        option("slimes", 6, "Giant slimes.");
        option("magma-cubes", 4, "Giant magma cubes.");
        option("size", 6, "Size of each (vanilla big is 4).");
        option("radius", 10, "Spread.");
    }

    @Override
    public void strike(Strike s) {
        for (Location l : Fx.circle(s.at(0, 25, 0), d("radius"), i("slimes"))) s.hunt(s.mob(l, Slime.class, sl -> sl.setSize(i("size"))), 40);
        for (Location l : Fx.circle(s.at(0, 30, 0), d("radius") / 2, i("magma-cubes"))) s.hunt(s.mob(l, MagmaCube.class, m -> m.setSize(i("size"))), 40);
        s.sound(s.at(), "entity.slime.squish", 10f, 0.4f);
    }
}
