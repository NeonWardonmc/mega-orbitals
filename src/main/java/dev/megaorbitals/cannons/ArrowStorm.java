package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;
import java.util.*;

/** Thousands of flaming arrows blot out the sun. */
public final class ArrowStorm extends Cannon {
    public ArrowStorm() {
        super("&f&lArrow Storm", Material.ARROW, "Thousands of flaming arrows", "blot out the sun.");
        warning(20, "#F0F0F0");
        option("radius", 18, "Area covered.");
        option("seconds", 6, "How long the volley lasts.");
        option("arrows-per-tick", 15, "Arrows each tick.");
        option("damage", 4.0, "Arrow damage.");
        option("flaming", true, "Set arrows on fire.");
    }

    @Override
    public void strike(Strike s) {
        Random rnd = new Random();
        double r = d("radius");
        s.every(1, i("seconds") * 20, t -> {
            for (int k = 0; k < i("arrows-per-tick"); k++) {
                Location l = s.at(rnd.nextGaussian() * r / 2, 30, rnd.nextGaussian() * r / 2);
                Arrow a = s.world.spawnArrow(l, new Vector(rnd.nextGaussian() * 0.05, -1, rnd.nextGaussian() * 0.05), 2.5f, 2f);
                a.setDamage(d("damage"));
                a.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
                if (b("flaming")) a.setFireTicks(400);
                if (s.shooter != null) a.setShooter(s.shooter);
                s.expire(s.tag(a), 100);
            }
            if (t % 4 == 0) s.sound(s.at(), "entity.arrow.shoot", 6f, 0.5f);
        });
    }
}
