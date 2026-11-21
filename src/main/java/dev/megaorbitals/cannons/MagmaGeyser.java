package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;

/** Magma geysers burst out of the ground at random, blasting anyone on top sky-high. */
public final class MagmaGeyser extends Cannon {
    public MagmaGeyser() {
        super("&c&lMagma Geysers", Material.MAGMA_CREAM, "Magma geysers burst out of the ground", "at random, launching anyone on top.");
        warning(20, "#FF4020");
        option("radius", 25, "Area of the geyser field.");
        option("seconds", 20, "How long geysers keep erupting.");
        option("geysers-per-second", 2, "New geysers each second.");
        option("launch", 3.0, "Launch strength.");
        option("damage", 6.0, "Burn damage.");
    }

    @Override
    public void strike(Strike s) {
        int every = Math.max(1, 20 / Math.max(1, i("geysers-per-second")));
        s.every(every, i("seconds") * 20 / every, t -> {
            Location g = s.randomPoint(d("radius"));
            s.every(2, 20, k -> {
                if (k < 10) {
                    Fx.p(s.world, Particle.LARGE_SMOKE, g, 4, 0.4, 0.02);
                    Fx.p(s.world, Particle.LAVA, g, 1, 0.3, 0);
                    if (k == 0) s.sound(g, "block.lava.pop", 3f, 0.5f);
                    return;
                }
                for (int y = 0; y < 12; y++) Fx.p(s.world, Particle.FLAME, g.clone().add(0, y, 0), 4, 0.4, 0.05);
                Fx.p(s.world, Particle.LAVA, g.clone().add(0, 3, 0), 6, 0.5, 0);
                if (k != 10) return;
                s.sound(g, "entity.generic.burn", 4f, 0.5f);
                for (LivingEntity le : s.victims(g, 2.5)) {
                    le.setVelocity(new Vector(0, d("launch"), 0));
                    le.setFireTicks(100);
                    s.damage(le, d("damage"));
                }
                if (s.breaks()) Blocks.set(g.clone().add(0, -1, 0).getBlock(), Material.MAGMA_BLOCK);
            });
        });
    }
}
