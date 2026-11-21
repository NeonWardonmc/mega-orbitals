package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import java.util.*;

/** The ground shakes violently, cracks open, and blocks are thrown into the air. */
public final class Earthquake extends Cannon {
    public Earthquake() {
        super("&6&lEarthquake", Material.CRACKED_STONE_BRICKS, "The ground shakes, splits open into", "cracks and throws everyone around.");
        warning(30, "#A06030");
        option("radius", 30, "Area of the quake.");
        option("seconds", 12, "How long the shaking lasts.");
        option("cracks", 7, "Number of cracks that split open.");
        option("crack-depth", 12, "How deep the cracks go.");
    }

    @Override
    public void strike(Strike s) {
        double r = d("radius");
        Random rnd = new Random();
        int depth = i("crack-depth");
        List<Vector> dirs = new ArrayList<>();
        for (int k = 0; k < i("cracks"); k++) {
            double a = rnd.nextDouble() * Math.PI * 2;
            dirs.add(new Vector(Math.cos(a), 0, Math.sin(a)));
        }
        s.every(2, i("seconds") * 10, t -> {
            if (t % 5 == 0) s.sound(s.at(), "entity.ravager.roar", 6f, 0.3f);
            if (t % 3 == 0) s.sound(s.at(), "block.basalt.break", 6f, 0.5f);
            for (LivingEntity le : s.victims(s.at(), r)) {
                le.setVelocity(le.getVelocity().add(new Vector(rnd.nextGaussian() * 0.25, rnd.nextDouble() * 0.25, rnd.nextGaussian() * 0.25)));
                if (t % 20 == 0) le.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 120, 0));
            }
            Fx.block(s.world, Particle.BLOCK, s.randomPoint(r), Material.DIRT, 20, 1);
            if (!s.breaks()) return;
            double len = Math.min(r, t * 0.8);
            for (Vector d : dirs) {
                Location g = s.surface(s.at().add(d.clone().multiply(len)));
                for (int y = -depth; y <= 1; y++) Blocks.set(g.clone().add(0, y, 0).getBlock(), Material.AIR);
            }
            for (int k = 0; k < 3; k++) {
                Block b = s.randomPoint(r).add(0, -1, 0).getBlock();
                if (b.getType().isAir() || !Blocks.breakable(b)) continue;
                FallingBlock fb = s.fallingBlock(b.getLocation().add(0.5, 1, 0.5), b.getType(), false);
                Blocks.set(b, Material.AIR);
                fb.setVelocity(new Vector(rnd.nextGaussian() * 0.1, 0.5 + rnd.nextDouble() * 0.4, rnd.nextGaussian() * 0.1));
            }
        });
    }
}
