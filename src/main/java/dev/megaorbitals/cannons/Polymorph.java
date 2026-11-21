package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/** Turns every mob nearby into a chicken. Players get chicken-weak and float like one. */
public final class Polymorph extends Cannon {
    public Polymorph() {
        super("&e&lPolymorph", Material.EGG, "Turns every mob nearby into a chicken.", "Players become as weak as one.");
        warning(20, "#FFF080");
        noBlockDamage();
        option("radius", 25, "Range.");
        option("include-bosses", false, "Also polymorph withers and wardens.");
        option("player-seconds", 30, "How long players stay chicken-weak.");
    }

    @Override
    public void strike(Strike s) {
        for (LivingEntity le : s.victims(s.at(), d("radius"))) {
            Location l = le.getLocation();
            Fx.p(s.world, Particle.CLOUD, l.clone().add(0, 1, 0), 20, 0.4, 0.05);
            if (le instanceof Player p) {
                int ticks = i("player-seconds") * 20;
                p.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, ticks, 4));
                p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, ticks, 0));
                p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, ticks, 1));
                Fx.title(p, "&e&lBAWK!", "&7You feel very... chicken.", 30);
                p.playSound(l, "entity.chicken.ambient", 1f, 1f);
                continue;
            }
            if (le instanceof Chicken || le instanceof ArmorStand) continue;
            if (!b("include-bosses") && (le instanceof Wither || le instanceof Warden || le instanceof EnderDragon)) continue;
            le.remove();
            s.world.spawn(l, Chicken.class);
            s.sound(l, "entity.chicken.egg", 1f, 1f);
        }
    }
}
