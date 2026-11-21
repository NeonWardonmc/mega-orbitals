package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/** A support strike for miners: haste, night vision and luck for everyone in range. */
public final class MinersBlessing extends Cannon {
    public MinersBlessing() {
        super("&e&lMiner's Blessing", Material.GOLDEN_PICKAXE, "Support for miners: haste, night vision", "and luck for everyone in range.");
        warning(5, "#FFE060");
        noBlockDamage();
        option("radius", 20, "Range.");
        option("minutes", 5, "How long the blessing lasts.");
        option("haste-level", 3, "Haste level.");
    }

    @Override
    public void strike(Strike s) {
        int ticks = i("minutes") * 1200;
        for (Player p : s.world.getPlayers()) {
            if (p.getLocation().distance(s.at()) > d("radius")) continue;
            p.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, ticks, i("haste-level") - 1));
            p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, ticks, 0));
            p.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, ticks, 2));
            p.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 20, 4));
            Fx.p(s.world, Particle.WAX_ON, p.getLocation().add(0, 1, 0), 25, 0.5, 0.1);
        }
        s.sound(s.at(), "block.amethyst_block.chime", 8f, 1.2f);
    }
}
