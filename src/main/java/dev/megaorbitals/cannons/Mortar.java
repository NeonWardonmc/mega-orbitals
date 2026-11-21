package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;

/** A mortar position at the target lobs high-arcing shells out in every direction. */
public final class Mortar extends Cannon {
    public Mortar() {
        super("&6&lMortar Battery", Material.CAULDRON, "A mortar position at the target lobs", "high-arcing shells out in every direction.");
        warning(20, "#C08020");
        option("range", 35, "Furthest shell distance.");
        option("seconds", 10, "How long it fires.");
        option("shells-per-second", 4, "Fire rate.");
        option("power", 3.0, "Shell explosion power.");
    }

    @Override
    public void strike(Strike s) {
        int every = Math.max(1, 20 / Math.max(1, i("shells-per-second")));
        Location gun = s.at(0, 1, 0);
        s.every(every, i("seconds") * 20 / every, t -> {
            double a = Math.random() * Math.PI * 2, dist = 6 + Math.random() * (i("range") - 6);
            int flight = 50;
            Vector v = new Vector(Math.cos(a) * dist / flight * 1.25, 1.1, Math.sin(a) * dist / flight * 1.25);
            TNTPrimed tnt = s.tnt(gun, (float) d("power"), 400);
            tnt.setVelocity(v);
            s.loop(1, k -> {
                if (!tnt.isValid() || k > 200) return false;
                if (k > 5 && tnt.isOnGround()) { tnt.setFuseTicks(1); return false; }
                Fx.p(s.world, Particle.SMOKE, tnt.getLocation(), 1, 0, 0);
                return true;
            });
            s.sound(gun, "entity.generic.explode", 4f, 1.8f);
            Fx.p(s.world, Particle.LARGE_SMOKE, gun, 5, 0.3, 0.05);
        });
    }
}
