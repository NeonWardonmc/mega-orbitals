package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;

/** Lightning hits everyone in range, launches them into the sky, then slams them back down. */
public final class Thor extends Cannon {
    public Thor() {
        super("&9&lThor's Judgement", Material.MACE, "Lightning hits everyone, throws them", "into the sky, then slams them down.");
        warning(40, "#4060FF");
        option("radius", 30, "Range.");
        option("damage", 8.0, "Lightning damage.");
        option("launch", 2.8, "How high everyone is thrown.");
        option("slam-power", 3.0, "Explosion power when they hit the ground.");
    }

    @Override
    public void strike(Strike s) {
        for (LivingEntity le : s.victims(s.at(), d("radius"))) {
            s.world.strikeLightningEffect(le.getLocation());
            s.damage(le, d("damage"));
            le.setVelocity(new Vector(0, d("launch"), 0));
            s.later(25, () -> {
                if (!le.isValid()) return;
                le.setVelocity(new Vector(0, -4, 0));
                Fx.line(s.world, Particle.ELECTRIC_SPARK, le.getLocation(), le.getLocation().add(0, 20, 0), 0.5);
                s.loop(1, t -> {
                    if (!le.isValid() || t > 60) return false;
                    if (!le.isOnGround()) return true;
                    s.explode(le.getLocation(), (float) d("slam-power"), false);
                    s.world.strikeLightningEffect(le.getLocation());
                    return false;
                });
            });
        }
        s.soundWorld("item.trident.thunder", 1f, 0.5f);
    }
}
