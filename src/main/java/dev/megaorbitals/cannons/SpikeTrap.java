package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;
import java.util.*;

/** Stone spikes erupt from the ground under every target and all across the area. */
public final class SpikeTrap extends Cannon {
    public SpikeTrap() {
        super("&7&lSpike Eruption", Material.POINTED_DRIPSTONE, "Stone spikes erupt out of the ground", "under every target and across the area.");
        warning(20, "#A08060");
        option("radius", 20, "Range.");
        option("damage", 14.0, "Damage to anyone impaled.");
        option("extra-spikes", 40, "Random spikes across the area.");
        option("spike-height", 4, "Height of each spike.");
    }

    @Override
    public void strike(Strike s) {
        for (LivingEntity le : s.victims(s.at(), d("radius"))) {
            spike(s, le.getLocation());
            s.damage(le, d("damage"));
            le.setVelocity(new Vector(0, 1.3, 0));
        }
        Random rnd = new Random();
        s.every(1, i("extra-spikes"), t -> spike(s, s.randomPoint(d("radius"))));
    }

    private void spike(Strike s, Location at) {
        Location g = s.surface(at);
        Fx.block(s.world, Particle.BLOCK, g, Material.DRIPSTONE_BLOCK, 20, 0.4);
        s.sound(g, "block.pointed_dripstone.land", 3f, 0.6f);
        if (!s.breaks()) return;
        int h = i("spike-height");
        for (int y = 0; y < h; y++) {
            Block b = g.clone().add(0, y, 0).getBlock();
            if (!b.getType().isAir()) break;
            Blocks.put(b, y == h - 1 ? Material.POINTED_DRIPSTONE : Material.DRIPSTONE_BLOCK, false);
        }
    }
}
