package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;

/** Support: drops saddled, diamond-armoured warhorses tamed to you. */
public final class Cavalry extends Cannon {
    public Cavalry() {
        super("&6&lCavalry Drop", Material.DIAMOND_HORSE_ARMOR, "Support: drops saddled, diamond-armoured", "warhorses already tamed to you.");
        warning(10, "#C09050");
        noBlockDamage();
        option("horses", 4, "Number of horses.");
        option("speed", 0.35, "Horse movement speed (vanilla average is about 0.22).");
        option("health", 40.0, "Horse health.");
    }

    @Override
    public void strike(Strike s) {
        for (Location l : Fx.circle(s.at(), 3, i("horses"))) {
            s.world.spawn(s.surface(l), Horse.class, h -> {
                h.setTamed(true);
                if (s.shooter != null) h.setOwner(s.shooter);
                h.getInventory().setSaddle(new ItemStack(Material.SADDLE));
                h.getInventory().setArmor(new ItemStack(Material.DIAMOND_HORSE_ARMOR));
                var sp = h.getAttribute(Attribute.MOVEMENT_SPEED);
                if (sp != null) sp.setBaseValue(d("speed"));
                var hp = h.getAttribute(Attribute.MAX_HEALTH);
                if (hp != null) { hp.setBaseValue(d("health")); h.setHealth(d("health")); }
                h.setJumpStrength(1.0);
            });
        }
        s.sound(s.at(), "entity.horse.gallop", 8f, 1f);
    }
}
