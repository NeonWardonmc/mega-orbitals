package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;
import java.util.*;

/** Opens a black hole that sucks in players, mobs and the ground itself. */
public final class BlackHole extends Cannon {
    public BlackHole() {
        super("&0&lBlack Hole", Material.OBSIDIAN, "Opens a black hole that pulls in", "players, mobs and the ground itself.");
        warning(40, "#300040");
        option("radius", 30, "Pull range.");
        option("seconds", 12, "How long the black hole lasts.");
        option("pull", 0.6, "Pull strength.");
        option("core-damage", 8.0, "Damage every half second at the core.");
        option("blocks-per-tick", 6, "Ground blocks ripped up each tick.");
        option("collapse-power", 10.0, "Power of the final collapse.");
    }

    @Override
    public void strike(Strike s) {
        Location core = s.at(0, 6, 0);
        BlockDisplay ball = Fx.display(core, Material.BLACK_CONCRETE, 3f);
        List<FallingBlock> debris = new ArrayList<>();
        Random rnd = new Random();
        double r = d("radius");
        s.every(1, i("seconds") * 20, t -> {
            Fx.dustSphere(s.world, core, 2.5, Color.BLACK, 3f, 60);
            Fx.ring(s.world, Particle.PORTAL, core, 4 + rnd.nextDouble() * r * 0.5, 30);
            for (LivingEntity le : s.victims(core, r)) {
                double dist = le.getLocation().distance(core);
                Vector pull = Fx.dir(le.getLocation(), core).multiply(d("pull") * (1.5 - dist / r));
                le.setVelocity(le.getVelocity().multiply(0.5).add(pull));
                if (dist < 3 && t % 10 == 0) s.damage(le, d("core-damage"));
            }
            if (s.breaks()) {
                for (int k = 0; k < i("blocks-per-tick"); k++) {
                    Block b = s.randomPoint(r).add(0, -1, 0).getBlock();
                    if (b.getType().isAir() || !Blocks.breakable(b)) continue;
                    FallingBlock fb = s.fallingBlock(b.getLocation().add(0.5, 0.5, 0.5), b.getType(), true);
                    Blocks.set(b, Material.AIR);
                    fb.setGravity(false);
                    debris.add(fb);
                }
            }
            debris.removeIf(fb -> {
                if (!fb.isValid()) return true;
                if (fb.getLocation().distance(core) < 2.5) { fb.remove(); return true; }
                fb.setVelocity(Fx.dir(fb.getLocation(), core).multiply(0.8));
                return false;
            });
            if (t % 20 == 0) s.sound(core, "block.portal.ambient", 8f, 0.3f);
        });
        s.later(i("seconds") * 20L, () -> {
            ball.remove();
            debris.forEach(Entity::remove);
            s.explode(core, (float) d("collapse-power"), false);
        });
    }
}
