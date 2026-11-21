package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;

/** Raises stone towers around the target with a skeleton sniper perched on each. */
public final class Snipers extends Cannon {
    public Snipers() {
        super("&f&lSniper Towers", Material.BOW, "Raises stone towers around the target with", "a skeleton sniper perched on top of each.");
        warning(20, "#E0E0E0");
        option("towers", 4, "Number of towers.");
        option("radius", 12.0, "Distance of the towers from the target.");
        option("tower-height", 10, "Height of each tower.");
        option("lifetime-seconds", 120, "Snipers vanish after this long.");
    }

    @Override
    public void strike(Strike s) {
        int h = i("tower-height");
        for (Location l : Fx.circle(s.at(), d("radius"), i("towers"))) {
            Location g = s.surface(l);
            if (s.breaks()) {
                for (int y = 0; y < h; y++) for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++) {
                    Blocks.set(g.clone().add(x, y, z).getBlock(), y == h - 1 ? Material.STONE_BRICK_SLAB : Material.STONE_BRICKS);
                }
            }
            Location top = g.clone().add(0, s.breaks() ? h : 0, 0);
            Skeleton sk = s.mob(top, Skeleton.class, x -> {
                ItemStack bow = new ItemStack(Material.BOW);
                bow.addUnsafeEnchantment(Enchantment.POWER, 5);
                x.getEquipment().setItemInMainHand(bow);
                x.getEquipment().setHelmet(new ItemStack(Material.IRON_HELMET));
                x.getEquipment().setItemInMainHandDropChance(0);
                x.getEquipment().setHelmetDropChance(0);
            });
            s.hunt(sk, 50);
            s.expire(sk, i("lifetime-seconds") * 20L);
        }
        s.sound(s.at(), "entity.skeleton.ambient", 8f, 0.6f);
    }
}
