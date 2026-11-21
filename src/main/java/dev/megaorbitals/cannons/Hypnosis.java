package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import io.papermc.paper.entity.LookAnchor;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/** A hypnotic spiral forces everyone nearby to stare at it, unable to look away. */
public final class Hypnosis extends Cannon {
    public Hypnosis() {
        super("&5&lHypnosis", Material.ENDER_EYE, "A hypnotic spiral forces everyone nearby", "to stare at it. You can't look away.");
        warning(10, "#A040C0");
        noBlockDamage();
        option("radius", 30, "Range.");
        option("seconds", 10, "How long they are hypnotised.");
    }

    @Override
    public void strike(Strike s) {
        Location eye = s.at(0, 4, 0);
        s.every(1, i("seconds") * 20, t -> {
            for (int k = 0; k < 4; k++) {
                double a = t * 0.3 + k * Math.PI / 2;
                double r = 1.5 + (t % 20) * 0.1;
                Fx.dust(s.world, eye.clone().add(Math.cos(a) * r, Math.sin(a) * r, 0), k % 2 == 0 ? Color.PURPLE : Color.WHITE, 1.2f, 1, 0);
            }
            for (LivingEntity le : s.victims(eye, d("radius"))) {
                if (le instanceof Player p) p.lookAt(eye.getX(), eye.getY(), eye.getZ(), LookAnchor.EYES);
                else le.setRotation(le.getLocation().setDirection(Fx.dir(le.getEyeLocation(), eye)).getYaw(), 0);
                if (t % 20 == 0) le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 25, 1, false, false));
            }
            if (t % 20 == 0) s.sound(eye, "block.portal.ambient", 3f, 2f);
        });
    }
}
