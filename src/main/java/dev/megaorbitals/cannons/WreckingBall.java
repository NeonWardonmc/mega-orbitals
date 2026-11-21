package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import java.util.*;

/** A colossal wrecking ball on a chain from orbit swings round and round, smashing everything. */
public final class WreckingBall extends Cannon {
    public WreckingBall() {
        super("&8&lWrecking Ball", Material.IRON_CHAIN, "A colossal wrecking ball on a chain", "from orbit swings round, smashing everything.");
        warning(30, "#606060");
        option("radius", 14.0, "Swing radius.");
        option("seconds", 12, "How long it swings.");
        option("swings", 4, "Full circles in that time.");
        option("damage", 16.0, "Damage on hit.");
        option("ball-size", 4.0, "Size of the ball.");
    }

    @Override
    public void strike(Strike s) {
        double r = d("radius"), size = d("ball-size");
        Location anchor = s.at(0, 40, 0);
        BlockDisplay ball = Fx.display(s.at(r, 3, 0), Material.OBSIDIAN, (float) size);
        int ticks = i("seconds") * 20;
        Map<UUID, Integer> lastHit = new HashMap<>();
        s.every(1, ticks, t -> {
            double a = 2 * Math.PI * i("swings") * t / ticks;
            Location p = s.at(Math.cos(a) * r, size / 2 + 1, Math.sin(a) * r);
            if (t % 2 == 0) ball.teleport(p);
            if (t % 2 == 0) Fx.line(s.world, Particle.SMOKE, anchor, p, 1.0);
            for (LivingEntity le : s.victims(p, size / 2 + 1.5)) {
                if (t - lastHit.getOrDefault(le.getUniqueId(), -100) < 20) continue;
                lastHit.put(le.getUniqueId(), t);
                s.damage(le, d("damage"));
                le.setVelocity(Fx.dir(s.at(), le.getLocation()).multiply(2).setY(0.8));
                s.sound(p, "block.anvil.land", 3f, 0.5f);
            }
            if (s.breaks() && t % 2 == 0) {
                int rr = (int) Math.ceil(size / 2);
                for (int x = -rr; x <= rr; x++) for (int y = -rr; y <= rr; y++) for (int z = -rr; z <= rr; z++) {
                    Blocks.set(p.clone().add(x, y, z).getBlock(), Material.AIR);
                }
            }
            if (t == ticks - 1) ball.remove();
        });
    }
}
