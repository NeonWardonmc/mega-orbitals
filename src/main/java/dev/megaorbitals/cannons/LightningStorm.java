package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import java.util.*;

/** A raging thunderstorm whose bolts seek out players under it. */
public final class LightningStorm extends Cannon {
    public LightningStorm() {
        super("&e&lLightning Storm", Material.LIGHTNING_ROD, "A thunderstorm whose bolts", "hunt players caught under it.");
        warning(30, "#F0F060");
        option("radius", 50, "Storm radius.");
        option("seconds", 20, "Storm length.");
        option("bolts-per-second", 3, "Lightning bolts per second.");
        option("damage", 8.0, "Extra damage per bolt.");
    }

    @Override
    public void strike(Strike s) {
        Random rnd = new Random();
        int every = Math.max(1, 20 / Math.max(1, i("bolts-per-second")));
        double r = d("radius");
        s.every(every, i("seconds") * 20 / every, t -> {
            for (int k = 0; k < 30; k++) Fx.p(s.world, Particle.FALLING_WATER, s.at(rnd.nextGaussian() * r / 2, 25, rnd.nextGaussian() * r / 2), 1, 0, 0);
            List<Player> players = s.players(s.at(), r);
            Location l = !players.isEmpty() && rnd.nextInt(3) != 0
                    ? players.get(rnd.nextInt(players.size())).getLocation()
                    : s.randomPoint(r);
            if (s.breaks()) s.world.strikeLightning(l); else s.world.strikeLightningEffect(l);
            for (LivingEntity le : s.victims(l, 3)) s.damage(le, d("damage"));
        });
    }
}
