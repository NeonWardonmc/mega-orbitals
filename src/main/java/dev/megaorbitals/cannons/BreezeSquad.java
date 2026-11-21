package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;

/** A squad of breezes drops in and fills the air with wind charges. */
public final class BreezeSquad extends Cannon {
    public BreezeSquad() {
        super("&b&lBreeze Squad", Material.BREEZE_ROD, "A squad of breezes drops in and fills", "the air with wind charges.");
        warning(20, "#C0F0FF");
        option("breezes", 8, "Number of breezes.");
        option("radius", 8.0, "Spread.");
    }

    @Override
    public void strike(Strike s) {
        for (Location l : Fx.circle(s.at(0, 10, 0), d("radius"), i("breezes"))) s.hunt(s.mob(l, Breeze.class, null), 40);
        s.sound(s.at(), "entity.breeze.wind_burst", 10f, 0.8f);
    }
}
