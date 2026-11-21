package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;

/** A giant flaming meteor streaks across the sky and slams into the target. */
public final class Meteor extends Cannon {
    public Meteor() {
        super("&6&lMeteor", Material.MAGMA_BLOCK, "A giant burning meteor streaks in", "from the sky and leaves a crater.");
        warning(30, "#FF6000");
        option("size", 7.0, "Size of the meteor.");
        option("flight-ticks", 60, "How long it takes to arrive.");
        option("power", 12.0, "Impact explosion power.");
        option("crater-radius", 12, "Crater radius.");
    }

    @Override
    public void strike(Strike s) {
        double a = Math.random() * Math.PI * 2;
        Location target = s.at();
        Location start = target.clone().add(Math.cos(a) * 80, 100, Math.sin(a) * 80);
        double size = d("size");
        BlockDisplay rock = Fx.display(start, Material.MAGMA_BLOCK, (float) size);
        int flight = i("flight-ticks");
        s.sound(target, "entity.wither.shoot", 10f, 0.3f);
        s.every(1, flight + 1, t -> {
            double p = t / (double) flight;
            Location l = start.clone().add(target.toVector().subtract(start.toVector()).multiply(p));
            if (t % 2 == 0) rock.teleport(l);
            Fx.p(s.world, Particle.FLAME, l, 25, size / 2, 0.05);
            Fx.p(s.world, Particle.LARGE_SMOKE, l, 10, size / 2, 0.02);
            Fx.p(s.world, Particle.LAVA, l, 4, size / 2, 0);
            if (t < flight) return;
            rock.remove();
            s.explode(target, (float) d("power"), true);
            int cr = i("crater-radius");
            s.blocks(Blocks.box(target, cr + 1, cr + 1, cr + 1, (b, dx, dy, dz) -> {
                double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
                if (dist <= cr) Blocks.set(b, Material.AIR);
                else if (dist <= cr + 1.5 && dy < 0 && !b.getType().isAir()) Blocks.set(b, Math.random() < 0.3 ? Material.MAGMA_BLOCK : Material.BLACKSTONE);
            }));
            s.soundWorld("entity.generic.explode", 1f, 0.4f);
        });
    }
}
