package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import java.util.*;

/** Laser drones hover over their targets, tracking them and zapping them over and over. */
public final class DroneSwarm extends Cannon {
    public DroneSwarm() {
        super("&b&lDrone Swarm", Material.OBSERVER, "Laser drones hover over their targets,", "tracking them and zapping them over and over.");
        warning(20, "#40C0E0");
        noBlockDamage();
        option("drones", 6, "Number of drones.");
        option("seconds", 30, "How long the drones stay.");
        option("range", 40.0, "How far drones hunt.");
        option("damage", 3.0, "Damage per zap.");
    }

    @Override
    public void strike(Strike s) {
        Random rnd = new Random();
        Color cyan = Color.fromRGB(60, 220, 255);
        for (int k = 0; k < i("drones"); k++) {
            Location pos = s.at(rnd.nextGaussian() * 3, 10, rnd.nextGaussian() * 3);
            BlockDisplay drone = Fx.display(pos, Material.OBSERVER, 0.7f);
            int offset = k * 3;
            s.every(1, i("seconds") * 20, t -> {
                LivingEntity target = s.nearest(pos, d("range"));
                Location want = target != null ? target.getLocation().add(Math.cos(t * 0.05 + offset) * 3, 4, Math.sin(t * 0.05 + offset) * 3) : s.at(0, 10, 0);
                pos.add(want.toVector().subtract(pos.toVector()).multiply(0.08));
                if (t % 2 == 0) drone.teleport(pos);
                if (target != null && (t + offset) % 15 == 0) {
                    Fx.dustLine(s.world, pos, target.getLocation().add(0, 1, 0), cyan, 0.8f, 0.4);
                    s.damage(target, d("damage"));
                    s.sound(pos, "block.beacon.power_select", 1f, 2f);
                }
                if (t == i("seconds") * 20 - 1) drone.remove();
            });
        }
    }
}
