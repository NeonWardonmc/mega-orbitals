package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import java.util.*;

/** Waves of armoured zombies claw out of the ground, and nearby villagers turn too. */
public final class ZombieApocalypse extends Cannon {
    public ZombieApocalypse() {
        super("&2&lZombie Apocalypse", Material.ZOMBIE_HEAD, "Waves of armoured zombies rise from the", "ground. Nearby villagers turn too.");
        warning(30, "#308020");
        option("radius", 20, "Where zombies rise.");
        option("waves", 3, "Number of waves.");
        option("per-wave", 12, "Zombies per wave.");
        option("seconds-between-waves", 10, "Time between waves.");
        option("infect-villagers", true, "Turn nearby villagers into zombie villagers.");
    }

    @Override
    public void strike(Strike s) {
        Random rnd = new Random();
        if (b("infect-villagers")) {
            for (Entity e : s.world.getNearbyEntities(s.at(), 40, 20, 40)) if (e instanceof Villager v) v.zombify();
        }
        s.every(i("seconds-between-waves") * 20L, i("waves"), wave -> {
            s.soundWorld("entity.zombie.ambient", 1f, 0.5f);
            for (int k = 0; k < i("per-wave"); k++) {
                Location l = s.randomPoint(d("radius"));
                Fx.block(s.world, Particle.BLOCK, l, Material.DIRT, 30, 0.5);
                Zombie z = s.mob(l, Zombie.class, zz -> {
                    if (rnd.nextInt(4) == 0) zz.setBaby();
                    EntityEquipment eq = zz.getEquipment();
                    eq.setHelmet(new ItemStack(wave > 0 ? Material.IRON_HELMET : Material.LEATHER_HELMET));
                    eq.setChestplate(new ItemStack(wave > 1 ? Material.DIAMOND_CHESTPLATE : Material.CHAINMAIL_CHESTPLATE));
                    eq.setItemInMainHand(new ItemStack(wave > 1 ? Material.DIAMOND_SWORD : Material.IRON_SWORD));
                    eq.setHelmetDropChance(0); eq.setChestplateDropChance(0); eq.setItemInMainHandDropChance(0);
                });
                s.hunt(z, 60);
            }
        });
    }
}
