package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;

/** Stone pillars burst out of the ground at random, catapulting anyone standing on them. */
public final class Geomancer extends Cannon {
    public Geomancer() {
        super("&7&lGeomancer", Material.DEEPSLATE, "Stone pillars burst out of the ground at", "random, catapulting anyone on top.");
        warning(20, "#808080");
        option("radius", 20, "Area of the pillar field.");
        option("pillars", 40, "Number of pillars.");
        option("pillar-height", 6, "Height of each pillar.");
        option("launch", 2.4, "Launch strength.");
    }

    @Override
    public void strike(Strike s) {
        s.every(3, i("pillars"), t -> {
            Location g = s.randomPoint(d("radius"));
            int h = i("pillar-height");
            s.every(1, h, y -> {
                if (s.breaks()) {
                    for (int x = -1; x <= 0; x++) for (int z = -1; z <= 0; z++) Blocks.set(g.clone().add(x, y, z).getBlock(), Material.DEEPSLATE);
                }
                Fx.block(s.world, Particle.BLOCK, g.clone().add(0, y, 0), Material.DEEPSLATE, 6, 0.6);
            });
            for (LivingEntity le : s.victims(g, 2)) le.setVelocity(new Vector(0, d("launch"), 0));
            s.sound(g, "block.deepslate.place", 3f, 0.5f);
        });
    }
}
