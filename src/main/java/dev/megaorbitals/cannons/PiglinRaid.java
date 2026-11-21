package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;

/** A war band of crossbow piglins and brutes storms in. They won't turn into zombies. */
public final class PiglinRaid extends Cannon {
    public PiglinRaid() {
        super("&6&lPiglin War Band", Material.GOLD_INGOT, "A war band of crossbow piglins and brutes", "storms in. They won't turn into zombies.");
        warning(25, "#E0A020");
        option("piglins", 10, "Crossbow piglins.");
        option("brutes", 3, "Piglin brutes.");
        option("radius", 10.0, "Spread.");
    }

    @Override
    public void strike(Strike s) {
        for (int k = 0; k < i("piglins"); k++) {
            Piglin p = s.mob(s.randomPoint(d("radius")), Piglin.class, x -> {
                x.setImmuneToZombification(true);
                x.getEquipment().setItemInMainHand(new ItemStack(Material.CROSSBOW));
                x.getEquipment().setHelmet(new ItemStack(Material.GOLDEN_HELMET));
                x.getEquipment().setItemInMainHandDropChance(0);
                x.getEquipment().setHelmetDropChance(0);
            });
            s.hunt(p, 50);
        }
        for (int k = 0; k < i("brutes"); k++) s.hunt(s.mob(s.randomPoint(d("radius")), PiglinBrute.class, x -> x.setImmuneToZombification(true)), 50);
        s.sound(s.at(), "entity.piglin.angry", 10f, 0.7f);
    }
}
