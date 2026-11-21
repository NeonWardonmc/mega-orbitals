package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.*;

/** The Deep Dark spreads across the surface, with shriekers and darkness pulses. */
public final class Sculk extends Cannon {
    public Sculk() {
        super("&3&lSculk Infestation", Material.SCULK, "The Deep Dark slowly spreads across the land,", "with shriekers and pulses of darkness.");
        warning(30, "#052030");
        option("radius", 30, "How far the sculk spreads.");
        option("spread-speed", 150, "Blocks converted per tick.");
        option("shriekers", 6, "Number of sculk shriekers.");
        option("darkness-seconds", 40, "How long the darkness pulses last.");
    }

    @Override
    public void strike(Strike s) {
        World w = s.world;
        Random rnd = new Random();
        s.blocks(Blocks.throttle(Blocks.columns(s.at(), i("radius"), (x, z, dist) -> {
            if (!Blocks.loaded(w, x, z)) return 1;
            Location top = s.surface(new Location(w, x + 0.5, s.at().getY(), z + 0.5));
            Block g = top.getBlock().getRelative(0, -1, 0);
            Blocks.set(g, Material.SCULK);
            if (rnd.nextInt(30) == 0 && top.getBlock().getType().isAir()) Blocks.put(top.getBlock(), Material.SCULK_SENSOR, false);
            return 4;
        }), i("spread-speed")));
        for (int k = 0; k < i("shriekers"); k++) {
            Block b = s.randomPoint(d("radius") * 0.7).getBlock();
            if (s.breaks() && b.getType().isAir()) Blocks.put(b, Material.SCULK_SHRIEKER, false);
        }
        s.every(40, i("darkness-seconds") / 2, t -> {
            for (LivingEntity le : s.victims(s.at(), d("radius"))) le.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 60, 0));
            Fx.p(w, Particle.SCULK_SOUL, s.at(0, 1, 0), 60, d("radius") / 2, 0.02);
            s.sound(s.at(), "block.sculk_shrieker.shriek", 8f, 0.8f);
        });
    }
}
