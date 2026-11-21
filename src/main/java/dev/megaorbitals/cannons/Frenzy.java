package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.*;

/** Drives every mob nearby into a frenzy: they turn on each other in a free-for-all. */
public final class Frenzy extends Cannon {
    public Frenzy() {
        super("&c&lFrenzy", Material.ROTTEN_FLESH, "Drives every mob nearby into a frenzy.", "They turn on each other.");
        warning(10, "#C04040");
        noBlockDamage();
        option("radius", 35, "Range.");
        option("seconds", 30, "How long the frenzy lasts.");
    }

    @Override
    public void strike(Strike s) {
        Random rnd = new Random();
        s.every(40, i("seconds") / 2, t -> {
            List<Mob> mobs = new ArrayList<>();
            for (LivingEntity le : s.victims(s.at(), d("radius"))) if (le instanceof Mob m) mobs.add(m);
            if (mobs.size() < 2) return;
            for (Mob m : mobs) {
                Mob other = mobs.get(rnd.nextInt(mobs.size()));
                if (other != m) m.setTarget(other);
                m.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 50, 1, false, false));
                m.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 50, 1, false, false));
                Fx.p(s.world, Particle.ANGRY_VILLAGER, m.getLocation().add(0, m.getHeight() + 0.3, 0), 1, 0, 0);
            }
        });
        s.sound(s.at(), "entity.ravager.roar", 8f, 1f);
    }
}
