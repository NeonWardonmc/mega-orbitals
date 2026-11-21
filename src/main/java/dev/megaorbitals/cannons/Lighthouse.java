package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

/** A searing lighthouse beam sweeps round at head height, blinding and burning anyone it passes. */
public final class Lighthouse extends Cannon {
    public Lighthouse() {
        super("&e&lDeath Lighthouse", Material.SEA_LANTERN, "A searing beam sweeps round at head height,", "blinding and burning anyone it passes.");
        warning(20, "#FFFF80");
        noBlockDamage();
        option("range", 40.0, "Length of the beam.");
        option("seconds", 20, "How long it sweeps.");
        option("rotation-seconds", 4, "Seconds per full turn.");
        option("damage", 5.0, "Damage when the beam passes you.");
    }

    @Override
    public void strike(Strike s) {
        Location eye = s.at(0, 1.6, 0);
        BlockDisplay lamp = Fx.display(eye.clone().add(0, 0.5, 0), Material.SEA_LANTERN, 1.5f);
        Color glow = Color.fromRGB(255, 255, 160);
        int turn = i("rotation-seconds") * 20, ticks = i("seconds") * 20;
        double range = d("range");
        s.every(1, ticks, t -> {
            double a = 2 * Math.PI * t / turn;
            Vector dir = new Vector(Math.cos(a), 0, Math.sin(a));
            Fx.dustLine(s.world, eye, eye.clone().add(dir.clone().multiply(range)), glow, 2f, 0.8);
            for (LivingEntity le : s.victims(eye, range)) {
                Vector to = le.getLocation().toVector().subtract(eye.toVector()).setY(0);
                if (to.lengthSquared() < 0.5) continue;
                double angle = to.angle(dir);
                if (angle > 0.08 + 1.0 / to.length()) continue;
                s.damage(le, d("damage"));
                le.setFireTicks(60);
                le.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 0));
            }
            if (t % 20 == 0) s.sound(eye, "block.beacon.ambient", 4f, 1.5f);
            if (t == ticks - 1) lamp.remove();
        });
    }
}
