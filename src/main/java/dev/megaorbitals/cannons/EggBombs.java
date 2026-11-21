package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import java.util.*;

/** A barrage of eggs rains down on everyone. They're not normal eggs. They explode. */
public final class EggBombs extends Cannon {
    public EggBombs() {
        super("&e&lEgg Bombs", Material.EGG, "A barrage of eggs rains down on everyone.", "They're not normal eggs. They explode.");
        warning(15, "#FFF0C0");
        option("radius", 25, "Range.");
        option("volleys", 6, "Number of volleys.");
        option("eggs-per-target", 3, "Eggs per target per volley.");
        option("power", 1.6, "Explosion power per egg.");
    }

    @Override
    public void strike(Strike s) {
        Random rnd = new Random();
        s.every(10, i("volleys"), t -> {
            for (LivingEntity le : s.victims(s.at(), d("radius"))) {
                for (int k = 0; k < i("eggs-per-target"); k++) {
                    Location from = le.getLocation().add(rnd.nextGaussian() * 3, 15, rnd.nextGaussian() * 3);
                    Egg egg = s.world.spawn(from, Egg.class, e -> e.setVelocity(Fx.dir(from, le.getLocation()).multiply(1.6)));
                    s.tag(egg);
                    Location[] last = {egg.getLocation()};
                    s.loop(1, k2 -> {
                        if (egg.isValid() && k2 < 100) { last[0] = egg.getLocation(); return true; }
                        s.explode(last[0], (float) d("power"), false);
                        return false;
                    });
                }
            }
            s.sound(s.at(), "entity.chicken.egg", 6f, 0.6f);
        });
    }
}
