package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;

/** A fleet of ghasts drifts in overhead and opens fire. */
public final class GhastFleet extends Cannon {
    public GhastFleet() {
        super("&f&lGhast Fleet", Material.GHAST_TEAR, "A fleet of ghasts drifts in overhead", "and opens fire on everyone below.");
        warning(30, "#F0F0F0");
        option("ghasts", 5, "Number of ghasts.");
        option("height", 22, "Height above the target.");
        option("radius", 14.0, "Spread of the fleet.");
    }

    @Override
    public void strike(Strike s) {
        for (Location l : Fx.circle(s.at(0, i("height"), 0), d("radius"), i("ghasts"))) s.hunt(s.mob(l, Ghast.class, null), 80);
        s.sound(s.at(), "entity.ghast.scream", 10f, 0.6f);
    }
}
