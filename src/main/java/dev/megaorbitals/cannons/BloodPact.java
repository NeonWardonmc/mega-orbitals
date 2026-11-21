package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;

/** You sacrifice half your health. Everyone near the target takes three times what you gave up. */
public final class BloodPact extends Cannon {
    public BloodPact() {
        super("&4&lBlood Pact", Material.REDSTONE_BLOCK, "You sacrifice half your health. Everyone near", "the target takes three times what you gave up.");
        warning(15, "#A00000");
        noBlockDamage();
        option("radius", 20, "Range.");
        option("sacrifice", 0.5, "Fraction of your current health you give up.");
        option("multiplier", 3.0, "Damage to each target = health you gave up x this.");
    }

    @Override
    public void strike(Strike s) {
        Player me = s.shooter;
        if (me == null || me.isDead()) return;
        double lost = me.getHealth() * Math.max(0, Math.min(0.95, d("sacrifice")));
        me.setHealth(Math.max(1, me.getHealth() - lost));
        me.playSound(me.getLocation(), "entity.player.hurt", 1f, 0.5f);
        Fx.p(me.getWorld(), Particle.DAMAGE_INDICATOR, me.getLocation().add(0, 1, 0), 15, 0.3, 0.1);
        double dmg = lost * d("multiplier");
        Color blood = Color.fromRGB(150, 0, 0);
        for (LivingEntity le : s.victims(s.at(), d("radius"))) {
            s.damage(le, dmg);
            Fx.dustLine(s.world, s.at(0, 10, 0), le.getLocation().add(0, 1, 0), blood, 1.5f, 0.5);
            Fx.dust(s.world, le.getLocation().add(0, 1, 0), blood, 2f, 20, 0.4);
        }
        s.actionBar(me, "&4You gave &c" + String.format("%.1f", lost / 2) + "❤ &4. They took &c" + String.format("%.1f", dmg / 2) + "❤ &4each.");
        s.sound(s.at(), "entity.warden.heartbeat", 8f, 0.6f);
    }
}
