package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import java.util.*;

/** An expanding ring of force that throws everyone and shatters fragile blocks. */
public final class Shockwave extends Cannon {
    public Shockwave() {
        super("&e&lShockwave", Material.GOAT_HORN, "An expanding ring of force throws", "everything back and shatters glass.");
        warning(30, "#FFE060");
        option("radius", 45, "How far the wave travels.");
        option("damage", 10.0, "Damage when the wave hits you.");
        option("knockback", 2.5, "How hard the wave throws you.");
    }

    private static boolean fragile(Material m) {
        String n = m.name();
        return n.contains("GLASS") || n.contains("LEAVES") || n.contains("LANTERN") || n.equals("TORCH")
                || n.contains("FLOWER") || n.equals("SHORT_GRASS") || n.equals("TALL_GRASS") || n.contains("ICE");
    }

    @Override
    public void strike(Strike s) {
        Location c = s.at();
        Set<UUID> hit = new HashSet<>();
        s.sound(c, "entity.generic.explode", 8f, 0.5f);
        s.every(1, i("radius"), t -> {
            double r = t + 1;
            for (Location l : Fx.circle(c, r, (int) (r * 3))) {
                Location g = s.surface(l);
                Fx.p(s.world, Particle.CLOUD, g, 1, 0.2, 0.05);
                if (s.breaks()) {
                    for (int y = 0; y < 6; y++) {
                        Block b = g.clone().add(0, y, 0).getBlock();
                        if (fragile(b.getType())) Blocks.set(b, Material.AIR);
                    }
                }
            }
            for (LivingEntity le : s.victims(c, r + 1.5)) {
                if (le.getLocation().distance(c) < r - 2 || !hit.add(le.getUniqueId())) continue;
                s.damage(le, d("damage"));
                le.setVelocity(Fx.dir(c, le.getLocation()).multiply(d("knockback")).setY(0.9));
            }
            if (t % 6 == 0) s.sound(c, "entity.breeze.wind_burst", 6f, 0.5f);
        });
    }
}
