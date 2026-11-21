package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import java.util.*;

/** Chains every target to the spot they're standing on. Wander too far and you're yanked back. */
public final class Tether extends Cannon {
    public Tether() {
        super("&7&lTether", Material.LEAD, "Chains every target to where they stand.", "Wander too far and you're yanked back.");
        warning(15, "#A0A0A0");
        noBlockDamage();
        option("radius", 25, "Range.");
        option("leash-length", 5.0, "How far they can move.");
        option("seconds", 25, "How long the tether lasts.");
        option("yank-damage", 1.0, "Damage each time you're yanked back.");
    }

    @Override
    public void strike(Strike s) {
        Map<LivingEntity, Location> anchors = new HashMap<>();
        for (LivingEntity le : s.victims(s.at(), d("radius"))) {
            anchors.put(le, le.getLocation());
            if (le instanceof Player p) Fx.title(p, "&7&lTETHERED", "&8You can't go far.", 30);
        }
        double len = d("leash-length");
        s.every(2, i("seconds") * 10, t -> anchors.forEach((le, a) -> {
            if (!le.isValid() || le.getWorld() != a.getWorld()) return;
            if (t % 3 == 0) Fx.line(s.world, Particle.SMOKE, a.clone().add(0, 0.3, 0), le.getLocation().add(0, 1, 0), 0.6);
            double dist = le.getLocation().distance(a);
            if (dist > len) {
                le.setVelocity(Fx.dir(le.getLocation(), a).multiply(Math.min(2.5, dist * 0.3)).setY(0.3));
                if (dist > len * 2.5) le.teleport(a);
                s.damage(le, d("yank-damage"));
                s.sound(le.getLocation(), "entity.leash_knot.break", 1f, 0.6f);
            }
        }));
    }
}
