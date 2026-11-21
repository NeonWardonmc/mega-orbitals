package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;

/** An orbital laser you steer with your crosshair. */
public final class Laser extends Cannon {
    public Laser() {
        super("&c&lSteerable Laser", Material.END_ROD, "An orbital laser that follows", "wherever you look. Burns everything.");
        warning(20, "#FF0000");
        option("seconds", 12, "How long you can steer the laser.");
        option("damage", 6.0, "Damage every half second.");
        option("beam-radius", 2.5, "Width of the burn area.");
        option("melt", true, "Melt blocks where the beam hits.");
    }

    @Override
    public void strike(Strike s) {
        Location[] hit = {s.at()};
        Color red = Color.fromRGB(255, 30, 30);
        double r = d("beam-radius");
        int secs = i("seconds");
        s.every(1, secs * 20, t -> {
            if (s.shooter != null && s.shooter.isOnline()) {
                Location l = s.plugin.target(s.shooter);
                if (l != null) hit[0] = l.add(0.5, 1, 0.5);
                s.actionBar(s.shooter, "&c&lLASER &7» steer with your crosshair &8(" + (secs - t / 20) + "s)");
            }
            Location h = hit[0];
            if (t % 2 == 0) {
                Fx.dustLine(s.world, h.clone().add(0, 80, 0), h, red, 2.5f, 0.7);
                Fx.p(s.world, Particle.LAVA, h, 3, 0.5, 0);
                Fx.p(s.world, Particle.FLAME, h, 8, 0.6, 0.05);
            }
            if (t % 10 == 0) {
                s.sound(h, "block.beacon.ambient", 4f, 2f);
                for (LivingEntity le : s.victims(h, r)) { s.damage(le, d("damage")); le.setFireTicks(100); }
            }
            if (b("melt") && s.breaks() && t % 3 == 0) {
                for (int x = -1; x <= 1; x++) for (int y = -2; y <= 0; y++) for (int z = -1; z <= 1; z++) {
                    Block b = h.clone().add(x, y, z).getBlock();
                    if (!b.getType().isAir()) Blocks.set(b, Material.AIR);
                }
                Block below = h.clone().add(0, -3, 0).getBlock();
                if (below.getType().isSolid()) Blocks.set(below, Material.MAGMA_BLOCK);
            }
        });
    }
}
