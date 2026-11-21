package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;

/** Drains the life out of everyone nearby and feeds it to you. */
public final class Vampire extends Cannon {
    public Vampire() {
        super("&4&lVampiric Drain", Material.REDSTONE, "Drains the life out of everyone nearby", "and feeds it to you.");
        warning(20, "#C00020");
        noBlockDamage();
        option("radius", 25, "Range.");
        option("seconds", 10, "How long the drain lasts.");
        option("damage", 2.0, "Damage to each target every half second.");
        option("lifesteal", 0.5, "Fraction of the damage you get back as health.");
    }

    @Override
    public void strike(Strike s) {
        Color blood = Color.fromRGB(160, 0, 20);
        s.every(10, i("seconds") * 2, t -> {
            double stolen = 0;
            for (LivingEntity le : s.victims(s.at(), d("radius"))) {
                s.damage(le, d("damage"));
                stolen += d("damage") * d("lifesteal");
                Location to = s.shooter != null && s.shooter.getWorld() == s.world ? s.shooter.getLocation().add(0, 1, 0) : s.at(0, 10, 0);
                Fx.dustLine(s.world, le.getLocation().add(0, 1, 0), to, blood, 1.2f, 0.5);
            }
            if (s.shooter != null && s.shooter.isOnline() && !s.shooter.isDead() && stolen > 0) {
                var max = s.shooter.getAttribute(Attribute.MAX_HEALTH);
                double cap = max == null ? 20 : max.getValue();
                s.shooter.setHealth(Math.min(cap, s.shooter.getHealth() + stolen));
                Fx.p(s.world, Particle.HEART, s.shooter.getLocation().add(0, 2, 0), 2, 0.3, 0);
            }
            if (t % 4 == 0) s.sound(s.at(), "entity.player.hurt_sweet_berry_bush", 6f, 0.5f);
        });
    }
}
