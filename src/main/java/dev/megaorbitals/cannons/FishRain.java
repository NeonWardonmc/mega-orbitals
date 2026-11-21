package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import java.util.*;

/** It's raining fish. Most are harmless... the pufferfish are not. */
public final class FishRain extends Cannon {
    public FishRain() {
        super("&3&lFish Rain", Material.PUFFERFISH, "It's raining fish. Most are harmless...", "the pufferfish are not.");
        warning(15, "#40A0C0");
        noBlockDamage();
        option("radius", 15, "Area covered.");
        option("fish", 60, "Number of fish.");
        option("pufferfish-chance", 0.35, "Share of fish that are pufferfish.");
        option("vanish-seconds", 30, "Fish vanish after this long.");
    }

    @Override
    public void strike(Strike s) {
        Random rnd = new Random();
        s.every(2, i("fish") / 3, t -> {
            for (int k = 0; k < 3; k++) {
                Location l = s.at(rnd.nextGaussian() * d("radius") / 2, 20, rnd.nextGaussian() * d("radius") / 2);
                Class<? extends Fish> type = rnd.nextDouble() < d("pufferfish-chance") ? PufferFish.class
                        : rnd.nextBoolean() ? Cod.class : rnd.nextBoolean() ? Salmon.class : TropicalFish.class;
                Fish f = s.mob(l, type, null);
                if (f instanceof PufferFish pf) pf.setPuffState(2);
                s.expire(f, i("vanish-seconds") * 20L);
            }
            if (t % 5 == 0) s.sound(s.at(), "entity.cod.flop", 6f, 1f);
        });
    }
}
