package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import java.util.*;

/** Roots burst from the ground and lock everyone in place, squeezing them. */
public final class Entangle extends Cannon {
    public Entangle() {
        super("&2&lEntangle", Material.MANGROVE_ROOTS, "Roots burst from the ground and lock", "everyone in place, squeezing them.");
        warning(15, "#507030");
        option("radius", 20, "Range.");
        option("seconds", 8, "How long they are held.");
        option("damage", 1.5, "Squeeze damage every second.");
    }

    @Override
    public void strike(Strike s) {
        Map<LivingEntity, Location> held = new HashMap<>();
        List<Block> roots = new ArrayList<>();
        for (LivingEntity le : s.victims(s.at(), d("radius"))) {
            held.put(le, le.getLocation());
            if (!s.breaks()) continue;
            Location f = le.getLocation().getBlock().getLocation();
            for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++) {
                if (x == 0 && z == 0) continue;
                Block b = f.clone().add(x, 0, z).getBlock();
                if (b.getType().isAir()) { Blocks.put(b, Material.MANGROVE_ROOTS, false); roots.add(b); }
            }
        }
        int ticks = i("seconds") * 20;
        s.every(1, ticks, t -> held.forEach((le, l) -> {
            if (!le.isValid()) return;
            if (le.getLocation().distanceSquared(l) > 0.04) {
                Location to = l.clone();
                to.setYaw(le.getLocation().getYaw());
                to.setPitch(le.getLocation().getPitch());
                le.teleport(to);
            }
            if (t % 20 == 0) {
                s.damage(le, d("damage"));
                Fx.block(s.world, Particle.BLOCK, le.getLocation(), Material.MANGROVE_ROOTS, 10, 0.4);
            }
        }));
        s.later(ticks, () -> roots.forEach(b -> { if (b.getType() == Material.MANGROVE_ROOTS) Blocks.put(b, Material.AIR, false); }));
        s.sound(s.at(), "block.roots.place", 8f, 0.5f);
    }
}
