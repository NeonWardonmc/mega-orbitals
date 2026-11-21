package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;
import java.util.*;

/** Two colossal walls slide in from either side and slam together, crushing anything between them. */
public final class Crusher extends Cannon {
    public Crusher() {
        super("&8&lHydraulic Crusher", Material.PISTON, "Two colossal walls slide in from both sides", "and slam together, crushing anything between.");
        warning(30, "#606060");
        noBlockDamage();
        option("width", 24.0, "Starting gap between the walls.");
        option("damage", 30.0, "Crush damage.");
        option("close-ticks", 40, "How long the walls take to close.");
    }

    @Override
    public void strike(Strike s) {
        Vector dir = s.shooter != null ? s.shooter.getLocation().getDirection().setY(0) : new Vector(1, 0, 0);
        if (dir.lengthSquared() < 0.01) dir = new Vector(1, 0, 0);
        Vector fwd = dir.clone().normalize();
        Vector side = new Vector(-fwd.getZ(), 0, fwd.getX());
        double half = d("width") / 2;
        int close = i("close-ticks");
        float yaw = (float) Math.toDegrees(Math.atan2(-side.getX(), side.getZ()));
        Location la = s.at(0, 4, 0).add(side.clone().multiply(-half)), lb = s.at(0, 4, 0).add(side.clone().multiply(half));
        la.setYaw(yaw); lb.setYaw(yaw);
        BlockDisplay a = Fx.display(la, Material.IRON_BLOCK, 16f, 8f, 1f);
        BlockDisplay b = Fx.display(lb, Material.IRON_BLOCK, 16f, 8f, 1f);
        Set<UUID> crushed = new HashSet<>();
        s.every(1, close + 1, t -> {
            double off = half * (1 - (double) t / close);
            Location pa = s.at(0, 4, 0).add(side.clone().multiply(-off - 0.5)), pb = s.at(0, 4, 0).add(side.clone().multiply(off + 0.5));
            pa.setYaw(yaw); pb.setYaw(yaw);
            a.teleport(pa); b.teleport(pb);
            for (LivingEntity le : s.victims(s.at(), half + 2)) {
                Vector rel = le.getLocation().toVector().subtract(s.at().toVector());
                double across = rel.dot(side), along = Math.abs(rel.dot(fwd));
                if (along > 8 || Math.abs(across) < off - 0.5) continue;
                le.setVelocity(side.clone().multiply(across > 0 ? -0.6 : 0.6));
                if (off < 1.2 && crushed.add(le.getUniqueId())) s.damage(le, d("damage"));
            }
            if (t == close) {
                s.sound(s.at(), "block.anvil.land", 10f, 0.4f);
                Fx.p(s.world, Particle.EXPLOSION, s.at(0, 2, 0), 6, 2, 0);
                s.later(30, () -> { a.remove(); b.remove(); });
            } else if (t % 8 == 0) s.sound(s.at(), "block.piston.extend", 6f, 0.5f);
        });
    }
}
