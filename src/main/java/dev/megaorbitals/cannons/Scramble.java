package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import java.util.*;

/** Everyone nearby swaps places with each other at random, several times. */
public final class Scramble extends Cannon {
    public Scramble() {
        super("&d&lScramble", Material.CHORUS_FRUIT, "Everyone nearby swaps places", "with each other at random. Repeatedly.");
        warning(20, "#E080FF");
        noBlockDamage();
        option("radius", 40, "Range.");
        option("swaps", 5, "Number of swaps.");
        option("seconds-between", 2, "Seconds between swaps.");
    }

    @Override
    public void strike(Strike s) {
        s.every(i("seconds-between") * 20L, i("swaps"), t -> {
            List<LivingEntity> all = s.victims(s.at(), d("radius"));
            if (all.size() < 2) return;
            List<Location> spots = new ArrayList<>();
            for (LivingEntity le : all) spots.add(le.getLocation());
            Collections.shuffle(spots);
            for (int k = 0; k < all.size(); k++) {
                LivingEntity le = all.get(k);
                Location to = spots.get(k).clone();
                to.setYaw(le.getLocation().getYaw());
                to.setPitch(le.getLocation().getPitch());
                Fx.p(s.world, Particle.REVERSE_PORTAL, le.getLocation().add(0, 1, 0), 20, 0.4, 0.05);
                le.teleport(to);
            }
            s.sound(s.at(), "entity.shulker.teleport", 8f, 1f);
        });
    }
}
