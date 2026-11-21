package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;
import java.util.*;

/** A colossal sandworm bursts out of the ground, diving in and out across the target, devouring everything. */
public final class Sandworm extends Cannon {
    public Sandworm() {
        super("&e&lSandworm", Material.SANDSTONE, "A colossal sandworm bursts out of the ground,", "diving in and out, devouring everything.");
        warning(40, "#D0B060");
        option("length", 120, "Distance the worm travels.");
        option("segments", 14, "Body segments.");
        option("size", 3.0, "Body thickness.");
        option("damage", 14.0, "Damage when it hits you.");
    }

    @Override
    public void strike(Strike s) {
        Vector dir = s.shooter != null ? s.shooter.getLocation().getDirection().setY(0) : new Vector(1, 0, 0);
        if (dir.lengthSquared() < 0.01) dir = new Vector(1, 0, 0);
        Vector d = dir.normalize();
        int len = i("length"), segs = i("segments");
        double size = d("size");
        Location start = s.at().subtract(d.clone().multiply(len / 2.0));
        List<BlockDisplay> body = new ArrayList<>();
        for (int k = 0; k < segs; k++) body.add(Fx.display(start, k == 0 ? Material.SMOOTH_SANDSTONE : Material.SANDSTONE, (float) (size * (1 - k * 0.03))));
        List<Location> trail = new ArrayList<>();
        Map<UUID, Integer> lastHit = new HashMap<>();
        int ticks = len * 2;
        s.every(1, ticks, t -> {
            Location head = start.clone().add(d.clone().multiply(t * 0.5)).add(0, Math.sin(t * 0.08) * 9 - 2, 0);
            trail.add(0, head);
            if (trail.size() > segs * 3) trail.remove(trail.size() - 1);
            for (int k = 0; k < segs; k++) {
                int idx = Math.min(trail.size() - 1, k * 3);
                body.get(k).teleport(trail.get(idx));
            }
            Fx.block(s.world, Particle.BLOCK, head, Material.SAND, 8, size / 2);
            for (LivingEntity le : s.victims(head, size + 1)) {
                if (t - lastHit.getOrDefault(le.getUniqueId(), -100) < 20) continue;
                lastHit.put(le.getUniqueId(), t);
                s.damage(le, d("damage"));
                le.setVelocity(new Vector(0, 1.4, 0).add(d.clone().multiply(0.6)));
            }
            if (s.breaks() && t % 2 == 0) {
                int r = (int) Math.ceil(size / 2);
                for (int x = -r; x <= r; x++) for (int y = -r; y <= r; y++) for (int z = -r; z <= r; z++) {
                    if (x * x + y * y + z * z <= r * r) Blocks.set(head.clone().add(x, y, z).getBlock(), Material.AIR);
                }
            }
            if (t % 15 == 0) s.sound(head, "entity.ravager.roar", 6f, 0.4f);
            if (t == ticks - 1) body.forEach(Entity::remove);
        });
    }
}
