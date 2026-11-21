package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;
import java.util.*;

/** A wandering tornado that sucks up players, mobs and blocks and flings them away. */
public final class Tornado extends Cannon {
    public Tornado() {
        super("&7&lTornado", Material.STRING, "A wandering tornado that sucks up", "everything and flings it away.");
        warning(30, "#B0B0B0");
        option("seconds", 20, "How long the tornado lasts.");
        option("pull-radius", 9, "How close you must be to get sucked in.");
        option("height", 30, "Height of the funnel.");
        option("speed", 0.25, "How fast it wanders.");
        option("rip-blocks", true, "Rip blocks out of the ground.");
    }

    @Override
    public void strike(Strike s) {
        Location pos = s.at();
        Vector drift = new Vector(1, 0, 0).rotateAroundY(Math.random() * Math.PI * 2);
        Random rnd = new Random();
        int h = i("height");
        double pr = d("pull-radius");
        s.every(1, i("seconds") * 20, t -> {
            drift.rotateAroundY(rnd.nextGaussian() * 0.1);
            pos.add(drift.clone().multiply(d("speed")));
            Location g = s.surface(pos);
            pos.setY(g.getY());
            for (int y = 0; y < h; y += 2) {
                double rad = 1 + y * 0.35;
                double a = t * 0.5 + y * 0.4;
                for (int k = 0; k < 3; k++) {
                    double aa = a + k * 2.09;
                    Fx.p(s.world, Particle.CLOUD, pos.clone().add(Math.cos(aa) * rad, y, Math.sin(aa) * rad), 1, 0.1, 0);
                }
            }
            for (LivingEntity le : s.victims(pos.clone().add(0, h / 2.0, 0), Math.max(pr, h / 2.0 + 2))) {
                Location l = le.getLocation();
                double hy = l.getY() - pos.getY();
                Vector flat = new Vector(pos.getX() - l.getX(), 0, pos.getZ() - l.getZ());
                if (flat.length() > pr + hy * 0.35) continue;
                Vector spin = new Vector(-flat.getZ(), 0, flat.getX()).normalize().multiply(0.6);
                Vector in = flat.lengthSquared() > 0.01 ? flat.normalize().multiply(0.25) : new Vector();
                le.setVelocity(hy > h - 4 ? spin.multiply(3).setY(0.8) : spin.add(in).setY(0.45));
                le.setFallDistance(0);
            }
            if (t % 10 == 0) s.sound(pos, "item.elytra.flying", 4f, 0.5f);
            if (b("rip-blocks") && s.breaks() && t % 2 == 0) {
                Block b = s.surface(pos.clone().add(rnd.nextGaussian() * 3, 0, rnd.nextGaussian() * 3)).add(0, -1, 0).getBlock();
                if (!b.getType().isAir() && Blocks.breakable(b)) {
                    FallingBlock fb = s.fallingBlock(b.getLocation().add(0.5, 1, 0.5), b.getType(), false);
                    Blocks.set(b, Material.AIR);
                    fb.setVelocity(new Vector(rnd.nextGaussian() * 0.6, 1.2, rnd.nextGaussian() * 0.6));
                }
            }
        });
    }
}
