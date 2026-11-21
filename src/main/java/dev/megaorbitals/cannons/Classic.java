package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import java.util.*;

/** The classic orbital strike cannon: rings of TNT dropped from the sky. */
public final class Classic extends Cannon {
    public Classic() {
        super("&c&lClassic Orbital", Material.FIRE_CHARGE, "The original orbital strike cannon.", "Rings of TNT dropped from the sky.");
        warning(30, "#FF2020");
        option("rings", 6, "Number of TNT circles.");
        option("ring-spacing", 3.0, "Blocks between circles.");
        option("height", 30, "Spawn height above the target.");
        option("power", 4.0, "Explosion power of each TNT.");
    }

    @Override protected double warningRadius() { return i("rings") * d("ring-spacing"); }

    @Override
    public void strike(Strike s) {
        int h = i("height");
        int fuse = Strike.fallTicks(h);
        float power = (float) d("power");
        Location sky = s.at(0, h, 0);
        s.tnt(sky, power, fuse);
        for (int r = 1; r <= i("rings"); r++) {
            double radius = r * d("ring-spacing");
            int n = (int) Math.max(4, Math.round(2 * Math.PI * radius / 2.0));
            for (Location l : Fx.circle(sky, radius, n)) s.tnt(l, power, fuse);
        }
        s.sound(s.at(), "entity.tnt.primed", 6f, 0.6f);
    }
}
