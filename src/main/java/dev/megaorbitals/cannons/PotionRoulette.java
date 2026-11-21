package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.*;

/** Everyone nearby gets a random mix of potion effects. Some good. Mostly not. */
public final class PotionRoulette extends Cannon {
    public PotionRoulette() {
        super("&d&lPotion Roulette", Material.BREWING_STAND, "Everyone nearby gets a random mix of", "potion effects. Some good. Mostly not.");
        warning(15, "#E060E0");
        noBlockDamage();
        option("radius", 20, "Range.");
        option("effects-each", 3, "Random effects per target.");
        option("seconds", 30, "How long each effect lasts.");
    }

    @Override
    public void strike(Strike s) {
        PotionEffectType[] pool = {PotionEffectType.SPEED, PotionEffectType.SLOWNESS, PotionEffectType.LEVITATION, PotionEffectType.BLINDNESS,
                PotionEffectType.NAUSEA, PotionEffectType.POISON, PotionEffectType.WITHER, PotionEffectType.WEAKNESS, PotionEffectType.GLOWING,
                PotionEffectType.JUMP_BOOST, PotionEffectType.INVISIBILITY, PotionEffectType.HUNGER, PotionEffectType.DARKNESS,
                PotionEffectType.MINING_FATIGUE, PotionEffectType.REGENERATION, PotionEffectType.STRENGTH, PotionEffectType.SLOW_FALLING,
                PotionEffectType.BAD_OMEN, PotionEffectType.INFESTED, PotionEffectType.OOZING, PotionEffectType.WEAVING};
        Random rnd = new Random();
        for (LivingEntity le : s.victims(s.at(), d("radius"))) {
            List<String> names = new ArrayList<>();
            for (int k = 0; k < i("effects-each"); k++) {
                PotionEffectType t = pool[rnd.nextInt(pool.length)];
                le.addPotionEffect(new PotionEffect(t, i("seconds") * 20, rnd.nextInt(3)));
                names.add(t.getKey().getKey().replace('_', ' '));
            }
            Fx.p(s.world, Particle.WITCH, le.getLocation().add(0, 1, 0), 30, 0.4, 0.1);
            if (le instanceof Player p) s.actionBar(p, "&d🎲 You rolled: &f" + String.join(", ", names));
        }
        s.sound(s.at(), "entity.witch.drink", 8f, 0.8f);
    }
}
