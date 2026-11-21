package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;

/** The ground collapses into a deep pit, layer by layer, taking everything with it. */
public final class Sinkhole extends Cannon {
    public Sinkhole() {
        super("&8&lSinkhole", Material.COARSE_DIRT, "The ground collapses into a deep pit,", "layer by layer.");
        warning(30, "#806040");
        option("radius", 10, "Radius of the pit.");
        option("depth", 60, "How deep the pit goes.");
        option("collapse-speed", 500, "Blocks removed per tick (lower = slower collapse).");
    }

    @Override
    public void strike(Strike s) {
        int r = i("radius"), depth = i("depth");
        Location mid = s.at(0, -depth / 2.0 + 3, 0);
        int r2 = r * r;
        s.blocks(Blocks.throttle(Blocks.box(mid, r, depth / 2 + 4, r, (b, dx, dy, dz) -> {
            if (dx * dx + dz * dz <= r2) Blocks.set(b, Material.AIR);
        }), i("collapse-speed")));
        s.every(5, 40, t -> {
            Fx.block(s.world, Particle.BLOCK, s.randomPoint(r), Material.DIRT, 30, r / 2.0);
            if (t % 4 == 0) s.sound(s.at(), "block.gravel.break", 8f, 0.4f);
        });
    }
}
