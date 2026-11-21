package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;
import java.util.*;

/** A wall of snow thunders across the target from your side, burying everything. */
public final class Avalanche extends Cannon {
    public Avalanche() {
        super("&f&lAvalanche", Material.SNOW_BLOCK, "A wall of snow thunders across the target", "from your side, burying everything.");
        warning(30, "#F0F8FF");
        option("width", 40, "Width of the avalanche front.");
        option("distance", 60, "How far it travels.");
        option("push", 1.0, "How hard it sweeps people along.");
    }

    @Override
    public void strike(Strike s) {
        Vector dir = s.shooter != null ? Fx.dir(s.shooter.getLocation(), s.at()).setY(0) : new Vector(1, 0, 0);
        if (dir.lengthSquared() < 0.01) dir = new Vector(1, 0, 0);
        dir.normalize();
        Vector side = new Vector(-dir.getZ(), 0, dir.getX());
        int dist = i("distance"), w = i("width");
        Location start = s.at().subtract(dir.clone().multiply(dist / 2.0));
        Random rnd = new Random();
        Vector d = dir;
        s.every(2, dist, t -> {
            Location front = start.clone().add(d.clone().multiply(t));
            for (int k = -w / 2; k <= w / 2; k += 2) {
                Location l = s.surface(front.clone().add(side.clone().multiply(k))).add(0, 6, 0);
                Fx.p(s.world, Particle.SNOWFLAKE, l, 4, 1, 0.1);
                if (s.breaks() && rnd.nextInt(3) == 0) s.fallingBlock(l, rnd.nextBoolean() ? Material.SNOW_BLOCK : Material.POWDER_SNOW, false)
                        .setVelocity(d.clone().multiply(0.4));
            }
            for (LivingEntity le : s.victims(front, w / 2.0)) {
                double along = le.getLocation().toVector().subtract(front.toVector()).dot(d);
                if (Math.abs(along) > 3) continue;
                le.setVelocity(d.clone().multiply(d("push")).setY(0.3));
                le.setFreezeTicks(Math.min(le.getMaxFreezeTicks() + 60, le.getFreezeTicks() + 40));
            }
            if (t % 5 == 0) s.sound(front, "block.snow.break", 8f, 0.4f);
        });
    }
}
