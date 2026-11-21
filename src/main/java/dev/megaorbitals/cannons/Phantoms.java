package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;

/** A circling squadron of giant phantoms dive-bombs everyone below. */
public final class Phantoms extends Cannon {
    public Phantoms() {
        super("&9&lPhantom Squadron", Material.PHANTOM_MEMBRANE, "A squadron of giant phantoms", "dive-bombs everyone below.");
        warning(20, "#3040A0");
        option("phantoms", 10, "Number of phantoms.");
        option("size", 4, "Phantom size (vanilla max natural is 0-2).");
        option("burn-in-daylight", false, "Let them burn in the sun.");
    }

    @Override
    public void strike(Strike s) {
        for (Location l : Fx.circle(s.at(0, 20, 0), 10, i("phantoms"))) {
            Phantom p = s.mob(l, Phantom.class, ph -> {
                ph.setSize(i("size"));
                ph.setShouldBurnInDay(b("burn-in-daylight"));
            });
            s.hunt(p, 60);
        }
        s.sound(s.at(), "entity.phantom.swoop", 10f, 0.5f);
    }
}
