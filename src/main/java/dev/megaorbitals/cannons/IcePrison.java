package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.*;

/** Encases every player and mob nearby in their own block of ice. */
public final class IcePrison extends Cannon {
    public IcePrison() {
        super("&b&lIce Prison", Material.PACKED_ICE, "Freezes everyone nearby inside", "their own block of ice.");
        warning(20, "#A0E0FF");
        option("radius", 20, "Range.");
        option("melt-seconds", 15, "Seconds until the ice melts (0 = never).");
    }

    @Override
    public void strike(Strike s) {
        List<Block> ice = new ArrayList<>();
        for (LivingEntity le : s.victims(s.at(), d("radius"))) {
            Location base = le.getLocation().getBlock().getLocation();
            le.teleport(base.clone().add(0.5, 0, 0.5).setDirection(le.getLocation().getDirection()));
            le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, i("melt-seconds") * 20 + 20, 5));
            le.setFreezeTicks(le.getMaxFreezeTicks() + 200);
            Fx.p(s.world, Particle.SNOWFLAKE, le.getLocation().add(0, 1, 0), 40, 0.6, 0.05);
            if (!s.breaks()) continue;
            for (int x = -1; x <= 1; x++) for (int y = -1; y <= 2; y++) for (int z = -1; z <= 1; z++) {
                boolean inside = x == 0 && z == 0 && (y == 0 || y == 1);
                if (inside) continue;
                Block b = base.clone().add(x, y, z).getBlock();
                if (b.getType().isAir() || !b.getType().isSolid()) { Blocks.set(b, Material.PACKED_ICE); ice.add(b); }
            }
        }
        s.sound(s.at(), "block.glass.place", 8f, 0.5f);
        if (i("melt-seconds") > 0) {
            s.later(i("melt-seconds") * 20L, () -> {
                for (Block b : ice) if (b.getType() == Material.PACKED_ICE) Blocks.put(b, Material.AIR, false);
                s.sound(s.at(), "block.glass.break", 6f, 1f);
            });
        }
    }
}
