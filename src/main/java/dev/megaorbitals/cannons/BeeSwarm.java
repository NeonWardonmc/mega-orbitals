package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;

/** A furious swarm of bees descends on everyone nearby. */
public final class BeeSwarm extends Cannon {
    public BeeSwarm() {
        super("&e&lKiller Bees", Material.BEEHIVE, "A furious swarm of bees descends", "on everyone nearby.");
        warning(20, "#FFD020");
        option("bees", 30, "Number of bees.");
        option("radius", 25, "How far the bees hunt.");
        option("remove-after-seconds", 60, "Bees vanish after this long.");
    }

    @Override
    public void strike(Strike s) {
        for (int k = 0; k < i("bees"); k++) {
            Bee bee = s.mob(s.at(Math.random() * 6 - 3, 6 + Math.random() * 4, Math.random() * 6 - 3), Bee.class, b -> b.setAnger(2400));
            s.hunt(bee, d("radius"));
            s.expire(bee, i("remove-after-seconds") * 20L);
        }
        s.sound(s.at(), "entity.bee.loop_aggressive", 10f, 1f);
    }
}
