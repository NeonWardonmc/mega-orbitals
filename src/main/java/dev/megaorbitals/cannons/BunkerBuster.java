package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;

/** A penetrator bomb that punches deep underground before detonating. Made for hidden bases. */
public final class BunkerBuster extends Cannon {
    public BunkerBuster() {
        super("&8&lBunker Buster", Material.NETHERITE_INGOT, "A penetrator bomb that punches deep", "underground before it detonates.");
        warning(30, "#404040");
        option("depth", 25, "How far underground it detonates.");
        option("power", 12.0, "Explosion power.");
    }

    @Override
    public void strike(Strike s) {
        Location sky = s.at(0, 50, 0);
        BlockDisplay bomb = Fx.display(sky, Material.NETHERITE_BLOCK, 1f, 3f, 1f);
        int depth = i("depth");
        s.every(1, 50 + depth + 1, t -> {
            Location l = t < 50 ? sky.clone().subtract(0, t, 0) : s.at(0, -(t - 50), 0);
            bomb.teleport(l);
            Fx.p(s.world, Particle.SMOKE, l, 4, 0.2, 0.02);
            if (t >= 50 && s.breaks()) {
                for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++) Blocks.set(l.clone().add(x, 0, z).getBlock(), Material.AIR);
            }
            if (t == 50) s.sound(l, "block.anvil.land", 8f, 0.5f);
            if (t == 50 + depth) {
                bomb.remove();
                s.explode(l, (float) d("power"), false);
                s.soundWorld("entity.generic.explode", 0.6f, 0.3f);
            }
        });
    }
}
