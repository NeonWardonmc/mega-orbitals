package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;

/** A legion of trident-wielding drowned rises out of the ground. */
public final class DrownedLegion extends Cannon {
    public DrownedLegion() {
        super("&3&lDrowned Legion", Material.TRIDENT, "A legion of trident-wielding drowned", "rises out of the ground.");
        warning(25, "#208080");
        option("drowned", 12, "Number of drowned.");
        option("radius", 12.0, "Spread.");
    }

    @Override
    public void strike(Strike s) {
        for (int k = 0; k < i("drowned"); k++) {
            Location l = s.randomPoint(d("radius"));
            Fx.p(s.world, Particle.SPLASH, l, 30, 0.5, 0.2);
            Drowned dr = s.mob(l, Drowned.class, x -> {
                x.getEquipment().setItemInMainHand(new ItemStack(Material.TRIDENT));
                x.getEquipment().setItemInMainHandDropChance(0);
                x.setShouldBurnInDay(false);
            });
            s.hunt(dr, 50);
        }
        s.sound(s.at(), "entity.drowned.ambient", 10f, 0.6f);
    }
}
