package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;

/** A crackling ring of electricity around the target. Anyone who touches it is zapped and thrown back in. */
public final class ElectricFence extends Cannon {
    public ElectricFence() {
        super("&e&lElectric Fence", Material.LIGHTNING_ROD, "A crackling ring of electricity. Anyone who", "touches it is zapped and thrown back in.");
        warning(15, "#F0F060");
        option("radius", 16.0, "Radius of the fence.");
        option("seconds", 45, "How long the fence stays on.");
        option("damage", 6.0, "Damage per zap.");
        option("place-rods", true, "Place lightning rod posts around the ring.");
    }

    @Override
    public void strike(Strike s) {
        double r = d("radius");
        if (b("place-rods") && s.breaks()) {
            for (Location l : Fx.circle(s.at(), r, (int) (r * 2 * Math.PI / 4))) {
                var b = s.surface(l).getBlock();
                if (b.getType().isAir()) Blocks.put(b, Material.LIGHTNING_ROD, false);
            }
        }
        s.every(2, i("seconds") * 10, t -> {
            for (double y = 0.3; y < 3; y += 0.9) Fx.ring(s.world, Particle.ELECTRIC_SPARK, s.at(0, y, 0), r, (int) (r * 3));
            for (LivingEntity le : s.victims(s.at(), r + 1.2)) {
                double dist = Math.hypot(le.getLocation().getX() - s.at().getX(), le.getLocation().getZ() - s.at().getZ());
                if (Math.abs(dist - r) > 1.2) continue;
                s.damage(le, d("damage"));
                le.setVelocity(Fx.dir(le.getLocation(), s.at()).setY(0).normalize().multiply(1.2).setY(0.4));
                Fx.p(s.world, Particle.ELECTRIC_SPARK, le.getLocation().add(0, 1, 0), 20, 0.3, 0.2);
                s.sound(le.getLocation(), "entity.lightning_bolt.impact", 1f, 2f);
            }
            if (t % 20 == 0) s.sound(s.at(), "block.beacon.ambient", 4f, 2f);
        });
    }
}
