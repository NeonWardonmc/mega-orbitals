package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;

/** A full illager raiding party appears: pillagers, vindicators, evokers and mounted ravagers. */
public final class Raid extends Cannon {
    public Raid() {
        super("&8&lRaiding Party", Material.OMINOUS_BOTTLE, "A full illager raid appears: pillagers,", "vindicators, evokers and ravagers.");
        warning(40, "#505050");
        option("pillagers", 8, "Pillagers.");
        option("vindicators", 5, "Vindicators.");
        option("evokers", 2, "Evokers.");
        option("ravagers", 2, "Ravagers (each with a rider).");
        option("radius", 12, "Spread.");
    }

    @Override
    public void strike(Strike s) {
        s.soundWorld("event.raid.horn", 2f, 1f);
        double r = d("radius");
        for (int k = 0; k < i("pillagers"); k++) s.hunt(s.mob(s.randomPoint(r), Pillager.class, null), 60);
        for (int k = 0; k < i("vindicators"); k++) s.hunt(s.mob(s.randomPoint(r), Vindicator.class, null), 60);
        for (int k = 0; k < i("evokers"); k++) s.hunt(s.mob(s.randomPoint(r), Evoker.class, null), 60);
        for (int k = 0; k < i("ravagers"); k++) {
            Location l = s.randomPoint(r);
            Ravager rav = s.mob(l, Ravager.class, null);
            rav.addPassenger(s.mob(l, Pillager.class, null));
            s.hunt(rav, 60);
        }
    }
}
