package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import java.util.*;

/** Tears open a rift to the End: end stone, obsidian pillars, shulkers and endermen. */
public final class EndRift extends Cannon {
    public EndRift() {
        super("&e&lEnd Rift", Material.END_STONE, "Tears open a rift to the End: end stone,", "obsidian pillars, shulkers and endermen.");
        warning(40, "#E0E0A0");
        option("radius", 22, "Size of the rift.");
        option("pillars", 4, "Obsidian pillars.");
        option("shulkers", 4, "Shulkers (on top of the pillars).");
        option("endermen", 8, "Endermen.");
    }

    @Override
    public void strike(Strike s) {
        int r = i("radius");
        s.blocks(Blocks.throttle(Blocks.surface(s.at(), r, Material.END_STONE), 400));
        List<Location> tops = new ArrayList<>();
        for (Location l : Fx.circle(s.at(), r * 0.6, i("pillars"))) {
            Location g = s.surface(l);
            int h = 8 + (int) (Math.random() * 8);
            if (s.breaks()) {
                for (int y = 0; y < h; y++) for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++) {
                    Blocks.set(g.clone().add(x, y, z).getBlock(), Material.OBSIDIAN);
                }
            }
            tops.add(g.clone().add(0, h, 0));
        }
        s.later(20, () -> {
            for (int k = 0; k < i("shulkers") && k < tops.size(); k++) s.mob(tops.get(k), Shulker.class, null);
            for (int k = 0; k < i("endermen"); k++) s.hunt(s.mob(s.randomPoint(r), Enderman.class, null), 40);
            s.soundWorld("entity.ender_dragon.growl", 1f, 0.8f);
        });
        s.every(4, 100, t -> Fx.p(s.world, Particle.PORTAL, s.at(0, 2, 0), 60, r / 2.0, 0.5));
    }
}
