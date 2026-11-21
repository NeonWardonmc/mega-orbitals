package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;

/** Silently deletes a perfect sphere of the world. No explosion. Just... gone. */
public final class Antimatter extends Cannon {
    public Antimatter() {
        super("&5&lAntimatter", Material.ENDER_EYE, "Deletes a perfect sphere of the world.", "No explosion. No drops. Just gone.");
        warning(60, "#8000FF");
        option("radius", 25, "Radius of the deleted sphere.");
        option("damage", 1000.0, "Damage to anything inside.");
    }

    @Override
    public void strike(Strike s) {
        Location c = s.at();
        double r = d("radius");
        s.every(1, 20, t -> {
            double rr = Math.max(0.5, r * (1 - t / 20.0));
            Fx.dustSphere(s.world, c, rr, Color.fromRGB(40, 0, 60), 3f, (int) Math.min(500, rr * rr * 1.5 + 10));
            Fx.sphere(s.world, Particle.REVERSE_PORTAL, c, rr, 80);
        });
        s.later(20, () -> {
            s.sound(c, "block.beacon.deactivate", 10f, 0.5f);
            s.sound(c, "entity.enderman.teleport", 10f, 0.5f);
            for (LivingEntity le : s.victims(c, r)) s.damage(le, d("damage"));
            s.blocks(Blocks.sphere(c, r, Material.AIR));
        });
    }
}
