package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;

/** A cavalry of baby zombies riding chickens. Fast, tiny and furious. */
public final class ChickenJockeys extends Cannon {
    public ChickenJockeys() {
        super("&e&lChicken Jockey Cavalry", Material.CHICKEN, "A cavalry of baby zombies riding chickens.", "Fast, tiny and furious.");
        warning(15, "#FFE080");
        option("jockeys", 12, "Number of chicken jockeys.");
        option("radius", 8.0, "Spread.");
    }

    @Override
    public void strike(Strike s) {
        for (Location l : Fx.circle(s.at(0, 15, 0), d("radius"), i("jockeys"))) {
            Chicken c = s.mob(l, Chicken.class, null);
            Zombie z = s.mob(l, Zombie.class, x -> { x.setBaby(); x.setShouldBurnInDay(false); });
            c.addPassenger(z);
            s.hunt(z, 40);
        }
        s.sound(s.at(), "entity.chicken.ambient", 10f, 0.6f);
    }
}
