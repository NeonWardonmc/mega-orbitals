package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/** The ground turns to quicksand. Anyone standing in it slowly sinks... and suffocates. */
public final class Quicksand extends Cannon {
    public Quicksand() {
        super("&e&lQuicksand", Material.SUSPICIOUS_SAND, "The ground turns to quicksand.", "Stand still and you sink. Then you suffocate.");
        warning(25, "#D0B060");
        option("radius", 12, "Size of the quicksand.");
        option("seconds", 20, "How long it pulls you down.");
        option("sink-speed", 0.08, "Blocks sunk per tick.");
    }

    @Override
    public void strike(Strike s) {
        int r = i("radius");
        s.blocks(Blocks.surface(s.at(), r, Material.SAND));
        s.every(2, i("seconds") * 10, t -> {
            for (LivingEntity le : s.victims(s.at(), r)) {
                le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 10, 3, false, false));
                le.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 10, 250, false, false));
                le.teleport(le.getLocation().subtract(0, d("sink-speed") * 2, 0));
                Fx.block(s.world, Particle.BLOCK, le.getLocation(), Material.SAND, 4, 0.3);
            }
            if (t % 15 == 0) s.sound(s.at(), "block.sand.step", 6f, 0.5f);
        });
    }
}
