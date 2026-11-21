package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import java.util.*;

/** Switches gravity off: players, mobs and chunks of ground float up... then it switches back on. */
public final class ZeroG extends Cannon {
    public ZeroG() {
        super("&b&lZero-G", Material.SHULKER_SHELL, "Turns gravity off. Everything floats up.", "Then gravity comes back.");
        warning(30, "#C0E0FF");
        option("radius", 25, "Range.");
        option("seconds", 10, "How long gravity is off.");
        option("lift-blocks", 120, "Ground blocks that float up.");
    }

    @Override
    public void strike(Strike s) {
        double r = d("radius");
        int ticks = i("seconds") * 20;
        List<FallingBlock> floating = new ArrayList<>();
        Random rnd = new Random();
        if (s.breaks()) {
            for (int k = 0; k < i("lift-blocks"); k++) {
                Block b = s.randomPoint(r).add(0, -1, 0).getBlock();
                if (b.getType().isAir() || !Blocks.breakable(b)) continue;
                FallingBlock fb = s.fallingBlock(b.getLocation().add(0.5, 0.1, 0.5), b.getType(), false);
                Blocks.set(b, Material.AIR);
                fb.setGravity(false);
                fb.setVelocity(new Vector(0, 0.05 + rnd.nextDouble() * 0.15, 0));
                floating.add(fb);
            }
        }
        s.every(10, ticks / 10, t -> {
            for (LivingEntity le : s.victims(s.at(), r)) le.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 15, 1, false, false));
            Fx.ring(s.world, Particle.END_ROD, s.at(0, 1, 0), r, (int) r * 3);
            for (FallingBlock fb : floating) if (fb.isValid()) fb.setTicksLived(1);
        });
        s.later(ticks, () -> {
            for (FallingBlock fb : floating) if (fb.isValid()) { fb.setGravity(true); fb.setVelocity(new Vector(0, -0.5, 0)); }
            s.sound(s.at(), "entity.elder_guardian.curse", 8f, 0.5f);
        });
    }
}
