package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/** Beams you down from orbit onto the target, safely. An orbital teleporter. */
public final class BeamMeUp extends Cannon {
    public BeamMeUp() {
        super("&b&lOrbital Teleporter", Material.ENDER_EYE, "Beams you down from orbit onto the target,", "safely. An orbital teleporter.");
        warning(10, "#80E0FF");
        noBlockDamage();
    }

    @Override
    public void strike(Strike s) {
        if (s.shooter == null) return;
        Location from = s.shooter.getLocation();
        Fx.dustLine(from.getWorld(), from.clone().add(0, 40, 0), from, Color.fromRGB(150, 230, 255), 2f, 0.6);
        Location to = s.at();
        to.setDirection(from.getDirection());
        s.shooter.teleport(to);
        s.shooter.setFallDistance(0);
        s.shooter.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 40, 0));
        Fx.dustLine(s.world, s.at(0, 40, 0), s.at(), Color.fromRGB(150, 230, 255), 2f, 0.6);
        Fx.p(s.world, Particle.END_ROD, s.at(0, 1, 0), 40, 0.4, 0.1);
        s.sound(s.at(), "block.beacon.power_select", 4f, 1.5f);
    }
}
