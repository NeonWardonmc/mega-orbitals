package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/** Jams cursed pumpkins onto everyone's heads. Their real helmet gets dropped on the ground. */
public final class PumpkinCurse extends Cannon {
    public PumpkinCurse() {
        super("&6&lPumpkin Curse", Material.CARVED_PUMPKIN, "Jams a cursed pumpkin onto everyone's head.", "It won't come off. Their helmet falls off.");
        warning(20, "#FF9020");
        noBlockDamage();
        option("radius", 20, "Range.");
        option("darkness-seconds", 20, "Darkness for the victims.");
    }

    @Override
    public void strike(Strike s) {
        for (Player p : s.players(s.at(), d("radius"))) {
            ItemStack old = p.getInventory().getHelmet();
            if (old != null && !old.getType().isAir()) s.world.dropItemNaturally(p.getLocation(), old);
            ItemStack pumpkin = new ItemStack(Material.CARVED_PUMPKIN);
            pumpkin.addUnsafeEnchantment(Enchantment.BINDING_CURSE, 1);
            pumpkin.addUnsafeEnchantment(Enchantment.VANISHING_CURSE, 1);
            p.getInventory().setHelmet(pumpkin);
            p.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, i("darkness-seconds") * 20, 0));
            Fx.title(p, "&6&l🎃", "&7It won't come off.", 30);
            Fx.p(s.world, Particle.SMOKE, p.getEyeLocation(), 20, 0.3, 0.02);
            p.playSound(p.getLocation(), "entity.witch.celebrate", 1f, 0.8f);
        }
    }
}
