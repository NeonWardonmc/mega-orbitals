package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/** A legion of ghost knights: you can only see their floating armour and swords. */
public final class PhantomLegion extends Cannon {
    public PhantomLegion() {
        super("&f&lGhost Knights", Material.IRON_HELMET, "A legion of ghost knights: you only see", "their floating armour and swords.");
        warning(30, "#C0C0E0");
        option("knights", 10, "Number of knights.");
        option("radius", 10.0, "Spread.");
    }

    @Override
    public void strike(Strike s) {
        for (int k = 0; k < i("knights"); k++) {
            Zombie z = s.mob(s.randomPoint(d("radius")), Zombie.class, x -> {
                x.setShouldBurnInDay(false);
                x.setAdult();
                x.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, PotionEffect.INFINITE_DURATION, 0, false, false));
                var eq = x.getEquipment();
                eq.setHelmet(new ItemStack(Material.IRON_HELMET));
                eq.setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
                eq.setLeggings(new ItemStack(Material.IRON_LEGGINGS));
                eq.setBoots(new ItemStack(Material.IRON_BOOTS));
                eq.setItemInMainHand(new ItemStack(Material.IRON_SWORD));
                eq.setHelmetDropChance(0); eq.setChestplateDropChance(0); eq.setLeggingsDropChance(0);
                eq.setBootsDropChance(0); eq.setItemInMainHandDropChance(0);
                x.setSilent(true);
            });
            s.hunt(z, 50);
            Fx.p(s.world, Particle.SOUL, z.getLocation().add(0, 1, 0), 20, 0.4, 0.02);
        }
        s.sound(s.at(), "entity.vex.ambient", 10f, 0.5f);
    }
}
