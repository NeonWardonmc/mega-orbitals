package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/** The ground turns into sticky tar: you can barely walk and you can't jump at all. */
public final class TarPit extends Cannon {
    public TarPit() {
        super("&8&lTar Pit", Material.MUD, "The ground turns to sticky tar.", "You can barely walk and can't jump.");
        warning(20, "#302820");
        option("radius", 18, "Size of the pit.");
        option("seconds", 25, "How long it sticks.");
    }

    @Override
    public void strike(Strike s) {
        int r = i("radius");
        s.blocks(Blocks.surface(s.at(), r, Material.MUD));
        s.every(10, i("seconds") * 2, t -> {
            for (LivingEntity le : s.victims(s.at(), r)) {
                le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 20, 4, false, false));
                le.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 20, 250, false, false));
                if (le instanceof Player p) p.setGliding(false);
            }
            Fx.block(s.world, Particle.BLOCK, s.randomPoint(r), Material.MUD, 15, 0.6);
            if (t % 4 == 0) s.sound(s.at(), "block.mud.step", 4f, 0.5f);
        });
    }
}
