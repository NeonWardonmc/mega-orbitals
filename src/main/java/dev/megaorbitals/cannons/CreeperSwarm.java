package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;

/** Charged creepers drop from orbit and sprint straight at the nearest player. */
public final class CreeperSwarm extends Cannon {
    public CreeperSwarm() {
        super("&a&lCreeper Swarm", Material.CREEPER_HEAD, "Charged creepers drop from orbit", "and hunt down the nearest player.");
        warning(30, "#40FF40");
        option("creepers", 14, "Number of creepers.");
        option("radius", 10, "Spread of the drop.");
        option("charged", true, "Spawn charged creepers.");
        option("fuse-ticks", 20, "Creeper fuse (vanilla is 30).");
        option("explosion-radius", 4, "Creeper explosion size (vanilla is 3).");
    }

    @Override
    public void strike(Strike s) {
        for (Location l : Fx.circle(s.at(0, 25, 0), d("radius"), i("creepers"))) {
            Creeper c = s.mob(l, Creeper.class, cr -> {
                cr.setPowered(b("charged"));
                cr.setMaxFuseTicks(i("fuse-ticks"));
                cr.setExplosionRadius(i("explosion-radius"));
            });
            s.later(30, () -> s.hunt(c, 60));
        }
        s.sound(s.at(), "entity.creeper.primed", 10f, 0.5f);
    }
}
