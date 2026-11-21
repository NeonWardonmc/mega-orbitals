package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import java.util.*;

/** Fire rains from the sky and sets the entire area ablaze at once. */
public final class Firestorm extends Cannon {
    public Firestorm() {
        super("&6&lFirestorm", Material.BLAZE_POWDER, "Fire rains from the sky and", "sets the whole area ablaze.");
        warning(30, "#FF6010");
        option("radius", 30, "Area set on fire.");
        option("fire-chance", 0.5, "Chance each spot catches fire (0-1).");
        option("burn-seconds", 15, "How long creatures burn.");
    }

    @Override
    public void strike(Strike s) {
        double r = d("radius");
        World w = s.world;
        Random rnd = new Random();
        s.every(2, 40, t -> {
            for (int k = 0; k < 30; k++) Fx.p(w, Particle.FLAME, s.at(rnd.nextGaussian() * r / 2, 5 + rnd.nextDouble() * 20, rnd.nextGaussian() * r / 2), 1, 0, 0.1);
            for (LivingEntity le : s.victims(s.at(), r)) le.setFireTicks(i("burn-seconds") * 20);
        });
        s.blocks(Blocks.columns(s.at(), (int) r, (x, z, dist) -> {
            if (!Blocks.loaded(w, x, z) || rnd.nextDouble() > d("fire-chance")) return 1;
            Block top = s.surface(new Location(w, x + 0.5, s.at().getY(), z + 0.5)).getBlock();
            if (top.getType().isAir() && top.getRelative(0, -1, 0).getType().isSolid()) top.setType(Material.FIRE, true);
            return 4;
        }));
        s.sound(s.at(), "entity.blaze.shoot", 10f, 0.4f);
    }
}
