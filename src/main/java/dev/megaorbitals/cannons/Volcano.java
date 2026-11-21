package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;
import java.util.*;

/** Raises a volcano out of the ground, then it erupts lava bombs everywhere. */
public final class Volcano extends Cannon {
    public Volcano() {
        super("&c&lVolcano", Material.MAGMA_BLOCK, "Raises a volcano out of the ground,", "then it erupts for 15 seconds.");
        warning(40, "#FF3000");
        option("radius", 16, "Radius of the volcano's base.");
        option("height", 20, "Height of the volcano.");
        option("eruption-seconds", 15, "How long it erupts.");
        option("bombs-per-second", 8, "Lava bombs thrown each second.");
    }

    @Override
    public void strike(Strike s) {
        int r = i("radius"), h = i("height");
        Location base = s.at(0, -1, 0);
        World w = s.world;
        int by = base.getBlockY();
        Random rnd = new Random();
        s.blocks(Blocks.throttle(Blocks.columns(base, r, (x, z, dist) -> {
            int top = (int) (h * (1 - dist / r));
            boolean crater = dist < 3;
            for (int y = 0; y <= top; y++) {
                Block b = w.getBlockAt(x, by + y, z);
                Material m;
                if (crater && y >= top - 2) m = y == top - 2 ? Material.LAVA : Material.AIR;
                else m = rnd.nextInt(8) == 0 ? Material.MAGMA_BLOCK : (rnd.nextBoolean() ? Material.BASALT : Material.BLACKSTONE);
                Blocks.set(b, m);
            }
            return top + 1;
        }), 600));
        Location top = s.at(0, h, 0);
        int per = Math.max(1, i("bombs-per-second"));
        s.later(40, () -> {
            s.soundWorld("entity.generic.explode", 1f, 0.2f);
            s.every(20 / Math.min(20, per) + 0, i("eruption-seconds") * Math.min(20, per), t -> {
                Fx.p(w, Particle.LAVA, top, 10, 1.5, 0);
                Fx.p(w, Particle.CAMPFIRE_SIGNAL_SMOKE, top, 4, 1.5, 0.05);
                FallingBlock bomb = s.fallingBlock(top.clone().add(0, 1, 0), Material.MAGMA_BLOCK, rnd.nextInt(3) != 0);
                bomb.setHurtEntities(true);
                bomb.setDamagePerBlock(2f);
                bomb.setVelocity(new Vector(rnd.nextGaussian() * 0.5, 1.0 + rnd.nextDouble() * 0.8, rnd.nextGaussian() * 0.5));
                if (t % 10 == 0) s.sound(top, "block.lava.extinguish", 6f, 0.5f);
                if (t % 15 == 0) {
                    s.world.spawn(top.clone().add(0, 2, 0), SmallFireball.class, f -> {
                        f.setVelocity(new Vector(rnd.nextGaussian() * 0.3, 0.6, rnd.nextGaussian() * 0.3));
                        s.tag(f);
                    });
                }
            });
        });
    }
}
