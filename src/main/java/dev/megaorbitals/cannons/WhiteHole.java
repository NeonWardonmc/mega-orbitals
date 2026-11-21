package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;
import java.util.*;

/** The opposite of a black hole: blasts everything, and the ground, outward. */
public final class WhiteHole extends Cannon {
    public WhiteHole() {
        super("&f&lWhite Hole", Material.QUARTZ_BLOCK, "Pushes everything away from it,", "ripping up the ground and flinging it.");
        warning(40, "#FFFFFF");
        option("radius", 30, "Push range.");
        option("seconds", 8, "How long it lasts.");
        option("push", 1.2, "Push strength.");
        option("blocks-per-tick", 8, "Ground blocks flung each tick.");
    }

    @Override
    public void strike(Strike s) {
        Location core = s.at(0, 3, 0);
        Random rnd = new Random();
        double r = d("radius");
        s.every(1, i("seconds") * 20, t -> {
            Fx.sphere(s.world, Particle.END_ROD, core, 2 + (t % 10) * 0.6, 60);
            for (LivingEntity le : s.victims(core, r)) {
                le.setVelocity(Fx.dir(core, le.getLocation()).multiply(d("push")).setY(0.6));
            }
            if (s.breaks()) {
                for (int k = 0; k < i("blocks-per-tick"); k++) {
                    Block b = s.randomPoint(r * 0.6).add(0, -1, 0).getBlock();
                    if (b.getType().isAir() || !Blocks.breakable(b)) continue;
                    FallingBlock fb = s.fallingBlock(b.getLocation().add(0.5, 1, 0.5), b.getType(), false);
                    Blocks.set(b, Material.AIR);
                    Vector out = Fx.dir(core, fb.getLocation()).setY(0);
                    if (out.lengthSquared() < 0.01) out = new Vector(1, 0, 0);
                    out.normalize().multiply(1 + rnd.nextDouble());
                    fb.setVelocity(out.setY(0.8 + rnd.nextDouble() * 0.6));
                }
            }
            if (t % 10 == 0) s.sound(core, "entity.breeze.wind_burst", 6f, 0.6f);
        });
    }
}
