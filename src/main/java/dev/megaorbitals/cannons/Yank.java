package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;

/** A grappling beam hooks everyone near the target and yanks them through the air toward you. */
public final class Yank extends Cannon {
    public Yank() {
        super("&6&lGrapple Yank", Material.FISHING_ROD, "A grappling beam hooks everyone near the target", "and yanks them through the air toward you.");
        warning(10, "#C08040");
        noBlockDamage();
        option("radius", 20, "Range around the target.");
        option("strength", 2.5, "How hard they're yanked.");
    }

    @Override
    public void strike(Strike s) {
        Location to = s.shooter != null && s.shooter.getWorld() == s.world ? s.shooter.getLocation() : s.at(0, 10, 0);
        for (LivingEntity le : s.victims(s.at(), d("radius"))) {
            double dist = le.getLocation().distance(to);
            le.setVelocity(Fx.dir(le.getLocation(), to).multiply(Math.min(4, d("strength") * Math.max(0.3, dist / 25))).setY(0.6 + dist / 60));
            Fx.line(s.world, Particle.CRIT, le.getLocation().add(0, 1, 0), to.clone().add(0, 1, 0), 0.8);
        }
        s.sound(s.at(), "entity.fishing_bobber.retrieve", 8f, 0.6f);
    }
}
