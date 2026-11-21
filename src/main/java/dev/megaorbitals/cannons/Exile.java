package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import java.util.*;

/** Banishes every player nearby to a random spot thousands of blocks away. */
public final class Exile extends Cannon {
    public Exile() {
        super("&5&lExile", Material.END_PORTAL_FRAME, "Banishes everyone nearby to a random spot", "thousands of blocks away.");
        warning(40, "#6020A0");
        noBlockDamage();
        option("radius", 20, "Range.");
        option("min-distance", 2000, "Minimum distance they are sent.");
        option("max-distance", 5000, "Maximum distance they are sent.");
        option("players-only", true, "Only exile players.");
    }

    @Override
    public void strike(Strike s) {
        Random rnd = new Random();
        for (LivingEntity le : s.victims(s.at(), d("radius"))) {
            if (b("players-only") && !(le instanceof Player)) continue;
            double a = rnd.nextDouble() * Math.PI * 2;
            double dist = i("min-distance") + rnd.nextDouble() * Math.max(0, i("max-distance") - i("min-distance"));
            int x = (int) (le.getLocation().getX() + Math.cos(a) * dist);
            int z = (int) (le.getLocation().getZ() + Math.sin(a) * dist);
            Fx.p(s.world, Particle.PORTAL, le.getLocation(), 80, 0.6, 1);
            s.world.getChunkAtAsync(x >> 4, z >> 4).thenAccept(chunk -> {
                Location to = new Location(s.world, x + 0.5, s.world.getHighestBlockYAt(x, z) + 1, z + 0.5);
                le.teleportAsync(to);
                if (le instanceof Player p) Fx.title(p, "&5&lEXILED", "&7You are " + (int) dist + " blocks from home.", 60);
            });
        }
        s.sound(s.at(), "block.portal.travel", 6f, 1f);
    }
}
