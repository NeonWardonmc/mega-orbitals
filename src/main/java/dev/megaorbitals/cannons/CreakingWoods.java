package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import java.util.*;

/** A grove of pale oak trunks sprouts around you, and creakings stalk you between them. */
public final class CreakingWoods extends Cannon {
    public CreakingWoods() {
        super("&7&lCreaking Woods", Material.PALE_OAK_LOG, "A grove of pale oak trunks sprouts around you,", "and creakings stalk you between them.");
        warning(30, "#B0B0A0");
        option("radius", 14, "Size of the grove.");
        option("trunks", 18, "Number of pale trunks.");
        option("creakings", 5, "Number of creakings.");
    }

    @Override
    public void strike(Strike s) {
        Random rnd = new Random();
        if (s.breaks()) {
            for (int k = 0; k < i("trunks"); k++) {
                Location g = s.randomPoint(d("radius"));
                int h = 5 + rnd.nextInt(5);
                for (int y = 0; y < h; y++) Blocks.set(g.clone().add(0, y, 0).getBlock(), Material.PALE_OAK_LOG);
                for (int x = -2; x <= 2; x++) for (int z = -2; z <= 2; z++) for (int y = h - 1; y <= h; y++) {
                    if (Math.abs(x) + Math.abs(z) > 3) continue;
                    var b = g.clone().add(x, y, z).getBlock();
                    if (b.getType().isAir()) b.setType(Material.PALE_OAK_LEAVES, false);
                }
            }
        }
        for (int k = 0; k < i("creakings"); k++) s.hunt(s.mob(s.randomPoint(d("radius")), Creaking.class, null), 40);
        s.every(10, 30, t -> Fx.p(s.world, Particle.WHITE_ASH, s.at(0, 3, 0), 40, d("radius") / 2, 0));
        s.sound(s.at(), "entity.creaking.ambient", 10f, 0.6f);
    }
}
