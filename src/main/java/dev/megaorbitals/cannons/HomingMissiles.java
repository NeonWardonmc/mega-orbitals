package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;

/** Launches missiles that hunt down the nearest target and explode on contact. */
public final class HomingMissiles extends Cannon {
    public HomingMissiles() {
        super("&c&lHoming Missiles", Material.FIREWORK_ROCKET, "Missiles that hunt down", "the nearest target.");
        warning(20, "#FF4040");
        option("missiles", 10, "Number of missiles.");
        option("range", 60, "How far missiles look for targets.");
        option("speed", 1.1, "Missile speed (blocks per tick).");
        option("power", 3.0, "Explosion power.");
        option("lifetime-seconds", 15, "Missiles explode after this long anyway.");
    }

    @Override
    public void strike(Strike s) {
        double speed = d("speed");
        for (Location start : Fx.circle(s.at(0, 35, 0), 6, i("missiles"))) {
            Location pos = start.clone();
            Vector vel = new Vector(0, -0.5, 0);
            s.loop(1, t -> {
                LivingEntity target = s.nearest(pos, d("range"));
                Vector want = target != null ? Fx.dir(pos, target.getLocation().add(0, 1, 0)) : new Vector(0, -1, 0);
                vel.multiply(0.85).add(want.multiply(0.15 * speed));
                if (vel.length() > speed) vel.normalize().multiply(speed);
                pos.add(vel);
                Fx.p(s.world, Particle.FLAME, pos, 2, 0.05, 0.01);
                Fx.p(s.world, Particle.SMOKE, pos, 2, 0.05, 0.01);
                if (t % 10 == 0) s.sound(pos, "entity.firework_rocket.launch", 2f, 0.6f);
                boolean hit = pos.getBlock().getType().isSolid()
                        || (target != null && target.getLocation().add(0, 1, 0).distance(pos) < 1.5)
                        || t > i("lifetime-seconds") * 20;
                if (hit) { s.explode(pos, (float) d("power"), false); return false; }
                return true;
            });
        }
    }
}
