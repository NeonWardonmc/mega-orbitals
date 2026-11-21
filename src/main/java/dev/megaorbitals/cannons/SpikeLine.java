package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;
import java.util.*;

/** A line of stone spikes erupts one after another, racing out from the target the way you face. */
public final class SpikeLine extends Cannon {
    public SpikeLine() {
        super("&7&lSpike Line", Material.POINTED_DRIPSTONE, "A line of stone spikes erupts one after another,", "racing out from the target the way you face.");
        warning(15, "#A08060");
        option("length", 40, "Length of the line.");
        option("damage", 12.0, "Damage when a spike hits you.");
        option("vanish-seconds", 8, "Spikes crumble after this long (0 = stay).");
    }

    @Override
    public void strike(Strike s) {
        Vector dir = s.shooter != null ? s.shooter.getLocation().getDirection().setY(0) : new Vector(1, 0, 0);
        if (dir.lengthSquared() < 0.01) dir = new Vector(1, 0, 0);
        Vector d = dir.normalize();
        List<Block> placed = new ArrayList<>();
        Set<UUID> hit = new HashSet<>();
        s.every(1, i("length"), t -> {
            Location g = s.surface(s.at().add(d.clone().multiply(t)));
            int h = 2 + (t % 3);
            if (s.breaks()) {
                for (int y = 0; y < h; y++) {
                    Block b = g.clone().add(0, y, 0).getBlock();
                    if (!b.getType().isAir()) break;
                    Blocks.put(b, y == h - 1 ? Material.POINTED_DRIPSTONE : Material.DRIPSTONE_BLOCK, false);
                    placed.add(b);
                }
            }
            for (LivingEntity le : s.victims(g, 1.8)) {
                if (!hit.add(le.getUniqueId())) continue;
                s.damage(le, d("damage"));
                le.setVelocity(new Vector(0, 1.2, 0));
            }
            Fx.block(s.world, Particle.BLOCK, g, Material.DRIPSTONE_BLOCK, 10, 0.4);
            if (t % 2 == 0) s.sound(g, "block.pointed_dripstone.land", 2f, 0.6f);
        });
        if (i("vanish-seconds") > 0) s.later(i("length") + i("vanish-seconds") * 20L, () -> {
            for (Block b : placed) if (b.getType() == Material.POINTED_DRIPSTONE || b.getType() == Material.DRIPSTONE_BLOCK) Blocks.put(b, Material.AIR, false);
        });
    }
}
