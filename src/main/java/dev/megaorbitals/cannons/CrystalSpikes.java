package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import java.util.*;

/** Rings of razor-sharp amethyst crystals erupt outward from the impact. */
public final class CrystalSpikes extends Cannon {
    public CrystalSpikes() {
        super("&d&lCrystal Eruption", Material.AMETHYST_SHARD, "Rings of razor-sharp amethyst crystals", "erupt outward from the impact.");
        warning(20, "#C080FF");
        option("radius", 20, "How far the crystal rings spread.");
        option("damage", 7.0, "Damage when a ring hits you.");
        option("vanish-seconds", 10, "Crystals shatter after this long (0 = stay).");
    }

    @Override
    public void strike(Strike s) {
        List<Block> placed = new ArrayList<>();
        Set<UUID> hit = new HashSet<>();
        s.every(2, i("radius") / 2, t -> {
            double r = (t + 1) * 2;
            for (Location l : Fx.circle(s.at(), r, (int) (r * 3))) {
                Block b = s.surface(l).getBlock();
                Fx.p(s.world, Particle.END_ROD, b.getLocation().add(0.5, 0.5, 0.5), 1, 0.1, 0.02);
                if (s.breaks() && b.getType().isAir()) { b.setType(Material.AMETHYST_CLUSTER, false); placed.add(b); }
            }
            for (LivingEntity le : s.victims(s.at(), r + 1)) {
                if (le.getLocation().distance(s.at()) < r - 1.5 || !hit.add(le.getUniqueId())) continue;
                s.damage(le, d("damage"));
            }
            s.sound(s.at(), "block.amethyst_cluster.place", 6f, 0.6f + t * 0.1f);
        });
        if (i("vanish-seconds") > 0) {
            s.later(i("vanish-seconds") * 20L, () -> {
                for (Block b : placed) if (b.getType() == Material.AMETHYST_CLUSTER) b.setType(Material.AIR, false);
                s.sound(s.at(), "block.amethyst_block.break", 6f, 1f);
            });
        }
    }
}
