package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;

/** White-hot thermite slowly melts a glowing shaft straight down, leaving a pool of lava at the bottom. */
public final class Thermite extends Cannon {
    public Thermite() {
        super("&f&lThermite", Material.MAGMA_CREAM, "White-hot thermite melts a glowing shaft", "straight down, leaving lava at the bottom.");
        warning(20, "#FFF0C0");
        option("radius", 2, "Radius of the shaft.");
        option("depth", 60, "How deep it melts.");
        option("ticks-per-layer", 4, "Melting speed (lower = faster).");
        option("damage", 6.0, "Burn damage to anything in the shaft.");
    }

    @Override
    public void strike(Strike s) {
        int r = i("radius");
        Location top = s.at(0, -1, 0);
        int depth = i("depth");
        s.loop(i("ticks-per-layer"), t -> {
            Location layer = top.clone().subtract(0, t, 0);
            if (t > depth || layer.getY() <= s.world.getMinHeight() + 1) {
                if (s.breaks()) Blocks.set(layer.clone().add(0, 1, 0).getBlock(), Material.LAVA);
                return false;
            }
            for (int x = -r; x <= r; x++) for (int z = -r; z <= r; z++) {
                if (x * x + z * z > r * r) continue;
                Block b = layer.clone().add(x, 0, z).getBlock();
                if (s.breaks()) Blocks.set(b, Material.AIR);
                Block wall = layer.clone().add(x * 1.5, 0, z * 1.5).getBlock();
                if (s.breaks() && !wall.getType().isAir() && Math.random() < 0.2) Blocks.set(wall, Material.MAGMA_BLOCK);
            }
            Fx.p(s.world, Particle.LAVA, layer, 6, r / 2.0, 0);
            Fx.p(s.world, Particle.FLAME, layer, 10, r / 2.0, 0.05);
            for (LivingEntity le : s.victims(layer, r + 1)) { s.damage(le, d("damage")); le.setFireTicks(100); }
            if (t % 5 == 0) s.sound(layer, "block.fire.ambient", 4f, 0.5f);
            return true;
        });
    }
}
