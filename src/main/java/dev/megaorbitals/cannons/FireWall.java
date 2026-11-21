package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;

/** A ring of eternal fire seals off the area: an arena nobody can leave without burning. */
public final class FireWall extends Cannon {
    public FireWall() {
        super("&6&lRing of Fire", Material.NETHERRACK, "A ring of eternal fire seals off the area.", "An arena nobody leaves without burning.");
        warning(20, "#FF6000");
        option("radius", 16.0, "Radius of the ring.");
        option("width", 2, "Width of the burning ring.");
        option("seconds", 60, "How long the flame wall burns hot.");
        option("burn-seconds", 8, "How long you burn after crossing.");
    }

    @Override
    public void strike(Strike s) {
        double r = d("radius");
        int w = i("width");
        if (s.breaks()) {
            for (int k = 0; k < w; k++) {
                for (Location l : Fx.circle(s.at(), r + k, (int) ((r + k) * 2 * Math.PI))) {
                    Location g = s.surface(l);
                    Block ground = g.getBlock().getRelative(0, -1, 0);
                    Blocks.set(ground, Material.NETHERRACK);
                    if (g.getBlock().getType().isAir()) g.getBlock().setType(Material.FIRE, false);
                }
            }
        }
        s.every(4, i("seconds") * 5, t -> {
            for (double y = 0; y < 4; y += 1.2) Fx.ring(s.world, Particle.FLAME, s.at(0, y, 0), r + w / 2.0, (int) (r * 2));
            for (LivingEntity le : s.victims(s.at(), r + w + 1)) {
                double dist = Math.hypot(le.getLocation().getX() - s.at().getX(), le.getLocation().getZ() - s.at().getZ());
                if (Math.abs(dist - (r + w / 2.0)) <= w / 2.0 + 0.8) le.setFireTicks(i("burn-seconds") * 20);
            }
        });
        s.sound(s.at(), "item.firecharge.use", 10f, 0.5f);
    }
}
