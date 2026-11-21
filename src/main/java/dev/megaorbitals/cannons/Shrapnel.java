package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;

/** A fragmentation shell: the blast sprays razor shrapnel flat out in every direction. */
public final class Shrapnel extends Cannon {
    public Shrapnel() {
        super("&7&lFrag Shell", Material.FLINT, "A fragmentation shell. The blast sprays", "razor shrapnel flat out in every direction.");
        warning(20, "#A0A0A0");
        option("fragments", 120, "Number of fragments.");
        option("damage", 6.0, "Damage per fragment.");
        option("speed", 3.0, "Fragment speed.");
        option("power", 3.0, "Power of the central blast.");
    }

    @Override
    public void strike(Strike s) {
        Location c = s.at(0, 1.2, 0);
        s.explode(s.at(), (float) d("power"), false);
        int n = i("fragments");
        for (int k = 0; k < n; k++) {
            double a = 2 * Math.PI * k / n;
            Vector v = new Vector(Math.cos(a), Math.random() * 0.2 - 0.05, Math.sin(a));
            Arrow ar = s.world.spawnArrow(c.clone().add(v), v, (float) d("speed"), 4f);
            ar.setDamage(d("damage"));
            ar.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
            ar.setSilent(true);
            if (s.shooter != null) ar.setShooter(s.shooter);
            s.expire(s.tag(ar), 60);
        }
        s.sound(c, "entity.generic.explode", 8f, 1.4f);
    }
}
