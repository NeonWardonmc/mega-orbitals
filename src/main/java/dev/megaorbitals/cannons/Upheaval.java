package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;

/** Rips a whole disc of land out of the ground and lifts it into the sky as a floating island. */
public final class Upheaval extends Cannon {
    public Upheaval() {
        super("&2&lUpheaval", Material.DIRT, "Rips a disc of land out of the ground and", "lifts it into the sky. Passengers included.");
        warning(40, "#60A040");
        option("radius", 12, "Radius of the lifted island.");
        option("depth", 10, "How deep below the target the island goes.");
        option("above", 15, "How far above the target it includes (trees, houses...).");
        option("lift", 35, "How high the island is lifted.");
    }

    @Override
    public void strike(Strike s) {
        int r = i("radius"), depth = i("depth"), above = i("above");
        int lift = Math.max(i("lift"), depth + above + 2);
        int r2 = r * r;
        int half = (depth + above) / 2;
        Location mid = s.at(0, (above - depth) / 2.0, 0);
        for (LivingEntity le : s.victims(s.at(), r)) le.teleport(le.getLocation().add(0, lift + 0.5, 0));
        if (s.shooter != null && s.spared(s.shooter) && s.shooter.getWorld() == s.world && s.shooter.getLocation().distance(s.at()) < r) s.shooter.teleport(s.shooter.getLocation().add(0, lift + 0.5, 0));
        s.blocks(Blocks.box(mid, r, half + 1, r, (b, dx, dy, dz) -> {
            if (dx * dx + dz * dz > r2 || b.getType().isAir() || !Blocks.breakable(b)) return;
            double edge = Math.sqrt(dx * dx + dz * dz) / r;
            if (b.getY() < s.at().getBlockY() - depth * (1 - edge * edge)) return;
            Blocks.set(b.getRelative(0, lift, 0), b.getBlockData());
            Blocks.set(b, Material.AIR);
        }));
        s.sound(s.at(), "block.stone.break", 10f, 0.3f);
        Fx.p(s.world, Particle.CLOUD, s.at(), 200, r / 2.0, 0.1);
    }
}
