package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/** The ground turns to swamp mud and bogged archers ambush you with poison arrows. */
public final class BogAmbush extends Cannon {
    public BogAmbush() {
        super("&2&lBog Ambush", Material.MUD, "The ground turns to swamp mud and bogged", "archers ambush you with poison arrows.");
        warning(25, "#406020");
        option("bogged", 10, "Number of bogged archers.");
        option("radius", 12.0, "Spread.");
    }

    @Override
    public void strike(Strike s) {
        s.blocks(Blocks.surface(s.at(), (int) d("radius") + 3, Material.MUD));
        for (int k = 0; k < i("bogged"); k++) {
            Location l = s.randomPoint(d("radius"));
            Fx.block(s.world, Particle.BLOCK, l, Material.MUD, 30, 0.5);
            s.hunt(s.mob(l, Bogged.class, x -> x.setShouldBurnInDay(false)), 50);
        }
        for (LivingEntity le : s.victims(s.at(), d("radius"))) le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 1));
        s.sound(s.at(), "entity.bogged.ambient", 10f, 0.6f);
    }
}
