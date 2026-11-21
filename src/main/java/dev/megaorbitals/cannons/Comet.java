package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;
import java.util.*;

/** A comet skims low across the ground through the target, gouging a burning trench. */
public final class Comet extends Cannon {
    public Comet() {
        super("&b&lComet", Material.LIGHT_BLUE_GLAZED_TERRACOTTA, "A comet skims low across the ground", "through the target, gouging a burning trench.");
        warning(25, "#60C0FF");
        option("length", 100, "Length of its path.");
        option("trench-radius", 3, "Size of the trench.");
        option("damage", 25.0, "Damage to anything it hits.");
    }

    @Override
    public void strike(Strike s) {
        Vector dir = s.shooter != null ? s.shooter.getLocation().getDirection().setY(0) : new Vector(1, 0, 0);
        if (dir.lengthSquared() < 0.01) dir = new Vector(1, 0, 0);
        Vector step = dir.normalize().multiply(1.0);
        int len = i("length"), tr = i("trench-radius");
        Location start = s.at().subtract(step.clone().multiply(len / 2.0));
        BlockDisplay head = Fx.display(start, Material.PACKED_ICE, 3f);
        Set<UUID> hit = new HashSet<>();
        s.every(1, len / 2, t -> {
            Location p = s.surface(start.clone().add(step.clone().multiply(t * 2)));
            if (t % 1 == 0) head.teleport(p.clone().add(0, 1, 0));
            Fx.p(s.world, Particle.END_ROD, p, 20, 1, 0.05);
            Fx.p(s.world, Particle.FLAME, p, 15, 1.2, 0.05);
            Fx.line(s.world, Particle.FIREWORK, p, p.clone().subtract(step.clone().multiply(12)).add(0, 3, 0), 0.6);
            for (LivingEntity le : s.victims(p, tr + 1.5)) {
                if (!hit.add(le.getUniqueId())) continue;
                s.damage(le, d("damage"));
                le.setFireTicks(120);
                le.setVelocity(step.clone().multiply(1.5).setY(1));
            }
            if (s.breaks()) {
                for (int x = -tr; x <= tr; x++) for (int y = -tr; y <= tr; y++) for (int z = -tr; z <= tr; z++) {
                    if (x * x + y * y + z * z > tr * tr) continue;
                    Blocks.set(p.clone().add(x, y, z).getBlock(), Material.AIR);
                }
                var f = p.clone().add(0, -tr - 1, 0).getBlock();
                if (f.getType().isSolid() && Math.random() < 0.3) Blocks.set(f, Material.MAGMA_BLOCK);
            }
            if (t % 4 == 0) s.sound(p, "entity.firework_rocket.large_blast", 6f, 0.5f);
            if (t == len / 2 - 1) head.remove();
        });
    }
}
