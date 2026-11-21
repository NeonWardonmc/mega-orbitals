package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.*;

/** A freezing blizzard: water turns to ice, snow piles up and everyone slowly freezes. */
public final class Blizzard extends Cannon {
    public Blizzard() {
        super("&f&lBlizzard", Material.POWDER_SNOW_BUCKET, "Freezes water, buries the land in snow", "and slowly freezes everyone solid.");
        warning(30, "#E0F0FF");
        option("radius", 35, "Blizzard radius.");
        option("seconds", 25, "How long it lasts.");
        option("freeze-damage", 1.5, "Damage every second while frozen.");
    }

    @Override
    public void strike(Strike s) {
        double r = d("radius");
        World w = s.world;
        Random rnd = new Random();
        s.blocks(Blocks.columns(s.at(), (int) r, (x, z, dist) -> {
            if (!Blocks.loaded(w, x, z)) return 1;
            Location top = s.surface(new Location(w, x + 0.5, s.at().getY(), z + 0.5));
            Block ground = top.getBlock().getRelative(0, -1, 0);
            Material g = ground.getType();
            if (g == Material.WATER) Blocks.set(ground, Material.ICE);
            else if (g == Material.LAVA) Blocks.set(ground, Material.OBSIDIAN);
            else if (top.getBlock().getType().isAir() && g.isSolid()) Blocks.set(top.getBlock(), rnd.nextInt(4) == 0 ? Material.POWDER_SNOW : Material.SNOW);
            return 4;
        }));
        s.every(2, i("seconds") * 10, t -> {
            for (int k = 0; k < 40; k++) Fx.p(w, Particle.SNOWFLAKE, s.at(rnd.nextGaussian() * r / 2, 10 + rnd.nextDouble() * 10, rnd.nextGaussian() * r / 2), 1, 0, 0.1);
            for (LivingEntity le : s.victims(s.at(), r)) {
                le.setFreezeTicks(Math.min(le.getMaxFreezeTicks() + 100, le.getFreezeTicks() + 10));
                le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 30, 2, false, false));
                if (t % 10 == 0) s.damage(le, d("freeze-damage"));
            }
            if (t % 20 == 0) s.sound(s.at(), "item.elytra.flying", 4f, 1.5f);
        });
    }
}
