package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/** A support strike: everyone in range gets huge speed, jump and swim boosts. */
public final class SpeedZone extends Cannon {
    public SpeedZone() {
        super("&b&lOverdrive", Material.SUGAR, "Support: everyone in range gets huge", "speed, jump and swim boosts.");
        warning(5, "#80FFFF");
        noBlockDamage();
        option("radius", 15, "Range.");
        option("seconds", 60, "How long the boost lasts.");
        option("speed-level", 3, "Speed level.");
    }

    @Override
    public void strike(Strike s) {
        int ticks = i("seconds") * 20;
        for (Player p : s.world.getPlayers()) {
            if (p.getLocation().distance(s.at()) > d("radius")) continue;
            p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, ticks, i("speed-level") - 1));
            p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, ticks, 2));
            p.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, ticks, 0));
            p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, ticks, 0));
            Fx.p(s.world, Particle.CLOUD, p.getLocation(), 20, 0.4, 0.1);
        }
        Fx.ring(s.world, Particle.END_ROD, s.at(0, 0.5, 0), d("radius"), 80);
        s.sound(s.at(), "entity.player.levelup", 6f, 1.5f);
    }
}
