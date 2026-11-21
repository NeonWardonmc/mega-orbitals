package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;

/** A frozen legion of strays marches out of a snowstorm, firing slowness arrows. */
public final class FrozenLegion extends Cannon {
    public FrozenLegion() {
        super("&b&lFrozen Legion", Material.SNOW_BLOCK, "A frozen legion of strays marches out of", "a snowstorm, firing slowness arrows.");
        warning(25, "#C0E8FF");
        option("strays", 10, "Number of strays.");
        option("radius", 12.0, "Spread.");
        option("snow", true, "Cover the ground in snow.");
    }

    @Override
    public void strike(Strike s) {
        if (b("snow")) s.blocks(Blocks.surface(s.at(), (int) d("radius") + 4, Material.SNOW_BLOCK));
        for (int k = 0; k < i("strays"); k++) {
            Stray st = s.mob(s.randomPoint(d("radius")), Stray.class, x -> x.setShouldBurnInDay(false));
            s.hunt(st, 50);
        }
        s.every(4, 50, t -> Fx.p(s.world, Particle.SNOWFLAKE, s.at(0, 6, 0), 40, d("radius") / 2, 0.05));
        s.sound(s.at(), "entity.stray.ambient", 10f, 0.6f);
    }
}
