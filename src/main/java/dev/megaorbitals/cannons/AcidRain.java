package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.*;

/** Toxic rain that burns anyone caught outside and slowly eats away the land. */
public final class AcidRain extends Cannon {
    public AcidRain() {
        super("&a&lAcid Rain", Material.SLIME_BALL, "Toxic rain burns anyone not under a roof", "and slowly eats away the land.");
        warning(30, "#80FF20");
        option("radius", 35, "Rain radius.");
        option("seconds", 30, "How long it rains.");
        option("damage", 3.0, "Damage per second to anyone outside.");
        option("corrode-per-second", 40, "Blocks corroded each second.");
    }

    private static Material corrode(Material m) {
        String n = m.name();
        if (n.contains("LEAVES") || n.contains("FLOWER") || n.contains("GRASS") && !n.equals("GRASS_BLOCK") || n.contains("WHEAT")
                || n.contains("CARROTS") || n.contains("POTATOES") || n.contains("FERN")) return Material.AIR;
        if (m == Material.GRASS_BLOCK || m == Material.DIRT || m == Material.PODZOL) return Material.COARSE_DIRT;
        if (m == Material.COARSE_DIRT) return Material.GRAVEL;
        if (m == Material.STONE) return Material.COBBLESTONE;
        if (m == Material.COBBLESTONE) return Material.GRAVEL;
        if (n.contains("LOG") || n.contains("PLANKS")) return Material.AIR;
        return null;
    }

    @Override
    public void strike(Strike s) {
        double r = d("radius");
        Random rnd = new Random();
        s.every(2, i("seconds") * 10, t -> {
            for (int k = 0; k < 40; k++) {
                Location l = s.at(rnd.nextGaussian() * r / 2, 15 + rnd.nextDouble() * 10, rnd.nextGaussian() * r / 2);
                Fx.block(s.world, Particle.FALLING_DUST, l, Material.LIME_CONCRETE, 1, 0);
            }
            if (t % 10 != 0) return;
            for (LivingEntity le : s.victims(s.at(), r)) {
                Location l = le.getLocation();
                if (s.world.getHighestBlockYAt(l) > l.getBlockY() + 1) continue;
                s.damage(le, d("damage"));
                le.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 40, 1));
                Fx.p(s.world, Particle.SMOKE, l.add(0, 1, 0), 5, 0.3, 0.02);
            }
            if (!s.breaks()) return;
            for (int k = 0; k < i("corrode-per-second"); k++) {
                Location p = s.randomPoint(r);
                Block b = s.world.getHighestBlockAt(p);
                Material to = corrode(b.getType());
                if (to != null) Blocks.set(b, to);
            }
            s.sound(s.at(), "block.lava.extinguish", 3f, 1.5f);
        });
    }
}
