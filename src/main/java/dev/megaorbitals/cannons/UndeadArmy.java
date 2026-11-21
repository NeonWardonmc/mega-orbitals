package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;

/** Skeleton horsemen and wither skeleton knights charge in. */
public final class UndeadArmy extends Cannon {
    public UndeadArmy() {
        super("&f&lUndead Cavalry", Material.SKELETON_HORSE_SPAWN_EGG, "Skeleton horsemen and wither skeleton", "knights charge in.");
        warning(30, "#C0C0C0");
        option("horsemen", 6, "Skeleton archers on horseback.");
        option("knights", 6, "Wither skeleton knights.");
        option("radius", 10, "Spread.");
    }

    @Override
    public void strike(Strike s) {
        s.world.strikeLightningEffect(s.at());
        for (Location l : Fx.circle(s.at(), d("radius"), i("horsemen"))) {
            Location g = s.surface(l);
            SkeletonHorse horse = s.mob(g, SkeletonHorse.class, h -> h.setTamed(true));
            Skeleton rider = s.mob(g, Skeleton.class, sk -> {
                sk.getEquipment().setHelmet(new ItemStack(Material.IRON_HELMET));
                sk.getEquipment().setItemInMainHand(new ItemStack(Material.BOW));
                sk.getEquipment().setHelmetDropChance(0);
            });
            horse.addPassenger(rider);
            s.hunt(rider, 60);
        }
        for (Location l : Fx.circle(s.at(), d("radius") / 2, i("knights"))) {
            WitherSkeleton k = s.mob(s.surface(l), WitherSkeleton.class, w -> {
                w.getEquipment().setItemInMainHand(new ItemStack(Material.NETHERITE_SWORD));
                w.getEquipment().setChestplate(new ItemStack(Material.NETHERITE_CHESTPLATE));
                w.getEquipment().setItemInMainHandDropChance(0);
                w.getEquipment().setChestplateDropChance(0);
            });
            s.hunt(k, 60);
        }
        s.sound(s.at(), "entity.skeleton_horse.ambient", 10f, 0.5f);
    }
}
