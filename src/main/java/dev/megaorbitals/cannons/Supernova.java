package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import java.util.*;

/** A star explodes above the target, burning everything, and leaves a glowing core behind. */
public final class Supernova extends Cannon {
    public Supernova() {
        super("&e&lSupernova", Material.GLOWSTONE, "A star explodes above the target,", "burning everything it touches.");
        warning(50, "#FFFF80");
        option("radius", 35, "Final size of the blast sphere.");
        option("damage", 15.0, "Damage to anything the sphere touches.");
        option("burn-seconds", 10, "How long targets burn.");
        option("core-radius", 4, "Size of the glowing star core left behind.");
    }

    @Override
    public void strike(Strike s) {
        Location core = s.at(0, 10, 0);
        Set<UUID> hit = new HashSet<>();
        for (Player p : s.world.getPlayers()) {
            if (p.getLocation().distance(core) < d("radius") * 2) Fx.title(p, "&e&l✦", "", 10);
        }
        s.soundWorld("entity.generic.explode", 1f, 0.3f);
        s.every(1, 30, t -> {
            double r = d("radius") * (t + 1) / 30.0;
            Fx.sphere(s.world, Particle.END_ROD, core, r, (int) Math.min(600, r * r * 1.5));
            Fx.dustSphere(s.world, core, r * 0.9, Color.fromRGB(255, 200, 60), 3f, (int) Math.min(300, r * r));
            for (LivingEntity le : s.victims(core, r)) {
                if (!hit.add(le.getUniqueId())) continue;
                s.damage(le, d("damage"));
                le.setFireTicks(i("burn-seconds") * 20);
            }
        });
        s.later(30, () -> s.blocks(Blocks.sphere(core, d("core-radius"), Material.SHROOMLIGHT)));
    }
}
