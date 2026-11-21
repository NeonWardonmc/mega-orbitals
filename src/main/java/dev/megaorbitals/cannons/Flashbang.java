package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/** A blinding flash and a deafening ring. No damage, just total disorientation. */
public final class Flashbang extends Cannon {
    public Flashbang() {
        super("&f&lFlashbang", Material.WHITE_CONCRETE_POWDER, "A blinding flash and a deafening ring.", "No damage. Total disorientation.");
        warning(5, "#FFFFFF");
        noBlockDamage();
        option("radius", 25, "Range.");
        option("seconds", 6, "How long they're blinded.");
    }

    @Override
    public void strike(Strike s) {
        int ticks = i("seconds") * 20;
        Fx.p(s.world, Particle.FLASH, s.at(0, 1, 0), 3, 0.5, 0);
        for (Player p : s.players(s.at(), d("radius"))) {
            Fx.title(p, "&f█████████████", "&f█████████████", ticks / 2);
            p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, ticks, 0));
            p.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, ticks + 40, 0));
            p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, ticks, 1));
            p.playSound(p.getLocation(), "block.note_block.bit", 1f, 2f);
            s.every(5, ticks / 5, t -> p.playSound(p.getLocation(), "block.note_block.pling", 0.4f, 2f));
        }
        for (LivingEntity le : s.victims(s.at(), d("radius"))) if (le instanceof Mob m) m.setTarget(null);
        s.sound(s.at(), "entity.firework_rocket.blast", 10f, 2f);
    }
}
