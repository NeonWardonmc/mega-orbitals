package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;
import java.util.*;

/** Giant spikes of ice erupt from the ground, impaling and launching anyone standing on them. */
public final class Glacier extends Cannon {
    public Glacier() {
        super("&b&lGlacier Spikes", Material.PACKED_ICE, "Giant spikes of ice erupt from the ground,", "launching anyone standing on them.");
        warning(25, "#A0E8FF");
        option("radius", 20, "Area covered.");
        option("spikes", 7, "Number of ice spikes.");
        option("min-height", 12, "Shortest spike.");
        option("max-height", 26, "Tallest spike.");
        option("damage", 10.0, "Damage to anyone launched.");
    }

    @Override
    public void strike(Strike s) {
        Random rnd = new Random();
        for (int k = 0; k < i("spikes"); k++) {
            Location base = k == 0 ? s.surface(s.at()) : s.randomPoint(d("radius"));
            int h = i("min-height") + rnd.nextInt(Math.max(1, i("max-height") - i("min-height")));
            double br = 2.5 + rnd.nextDouble() * 1.5;
            for (LivingEntity le : s.victims(base, br + 1)) {
                s.damage(le, d("damage"));
                le.setVelocity(new Vector(0, 2.2, 0));
            }
            s.blocks(Blocks.throttle(Blocks.box(base.clone().add(0, h / 2.0, 0), (int) br + 1, h / 2 + 1, (int) br + 1, (b, dx, dy, dz) -> {
                int y = b.getY() - base.getBlockY();
                if (y < 0 || y > h) return;
                double rr = br * (1 - (double) y / h);
                if (dx * dx + dz * dz <= rr * rr) Blocks.set(b, rnd.nextInt(4) == 0 ? Material.BLUE_ICE : Material.PACKED_ICE);
            }), 400));
            s.sound(base, "block.glass.break", 6f, 0.4f);
        }
    }
}
