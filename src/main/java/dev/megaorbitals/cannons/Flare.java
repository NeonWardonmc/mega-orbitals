package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.*;

/** A blazing flare lights up the whole area like daytime and reveals every creature in it. */
public final class Flare extends Cannon {
    public Flare() {
        super("&e&lFlare", Material.TORCH, "A blazing flare lights up the area like", "daytime and reveals every creature in it.");
        warning(5, "#FFF0A0");
        noBlockDamage();
        option("radius", 24, "Lit area.");
        option("seconds", 90, "How long the light lasts.");
    }

    @Override
    public void strike(Strike s) {
        int r = i("radius");
        List<Block> lights = new ArrayList<>();
        for (int x = -r; x <= r; x += 6) for (int z = -r; z <= r; z += 6) {
            if (x * x + z * z > r * r) continue;
            for (int y = 1; y <= 2; y++) {
                Block b = s.surface(s.at(x, 0, z)).add(0, y, 0).getBlock();
                if (!b.getType().isAir()) continue;
                Blocks.put(b, Material.LIGHT, false);
                if (b.getBlockData() instanceof Levelled lv) { lv.setLevel(15); Blocks.put(b, lv, false); }
                lights.add(b);
                break;
            }
        }
        for (LivingEntity le : s.victims(s.at(), r)) le.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, i("seconds") * 20, 0));
        s.every(10, i("seconds") * 2, t -> Fx.p(s.world, Particle.FLAME, s.at(0, 12, 0), 6, 0.3, 0.02));
        s.later(i("seconds") * 20L, () -> lights.forEach(b -> { if (b.getType() == Material.LIGHT) Blocks.put(b, Material.AIR, false); }));
        s.sound(s.at(), "entity.firework_rocket.launch", 6f, 0.6f);
    }
}
