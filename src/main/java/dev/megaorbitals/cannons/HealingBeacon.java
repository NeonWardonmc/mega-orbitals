package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/** A support strike: fully heals every player in range, cleanses bad effects and buffs them. */
public final class HealingBeacon extends Cannon {
    public HealingBeacon() {
        super("&d&lHealing Beacon", Material.GOLDEN_APPLE, "A support strike: heals every player in", "range, cleanses debuffs and buffs them.");
        warning(10, "#FF80C0");
        noBlockDamage();
        option("radius", 20, "Range.");
        option("buff-seconds", 60, "How long the buffs last.");
    }

    private static PotionEffectType[] bad() { return new PotionEffectType[]{PotionEffectType.POISON, PotionEffectType.WITHER, PotionEffectType.SLOWNESS,
            PotionEffectType.WEAKNESS, PotionEffectType.BLINDNESS, PotionEffectType.DARKNESS, PotionEffectType.NAUSEA,
            PotionEffectType.MINING_FATIGUE, PotionEffectType.HUNGER, PotionEffectType.LEVITATION}; }

    @Override
    public void strike(Strike s) {
        int buff = i("buff-seconds") * 20;
        for (Player p : s.world.getPlayers()) {
            if (p.getLocation().distance(s.at()) > d("radius")) continue;
            var max = p.getAttribute(Attribute.MAX_HEALTH);
            p.setHealth(max == null ? 20 : max.getValue());
            p.setFoodLevel(20);
            p.setSaturation(20);
            p.setFireTicks(0);
            p.setFreezeTicks(0);
            for (PotionEffectType bad : bad()) p.removePotionEffect(bad);
            p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, buff, 1));
            p.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, buff, 3));
            p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, buff, 0));
            p.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, buff, 0));
            Fx.p(s.world, Particle.HEART, p.getLocation().add(0, 2, 0), 10, 0.5, 0);
            s.actionBar(p, "&d❤ Healed by an orbital beacon");
        }
        s.every(2, 30, t -> {
            Fx.dustLine(s.world, s.at(0, 60, 0), s.at(), Color.fromRGB(255, 200, 230), 2.5f, 1);
            Fx.dustRing(s.world, s.at(0, 0.3, 0), d("radius") * t / 30.0, Color.fromRGB(255, 120, 200), 1.5f);
        });
        s.sound(s.at(), "block.beacon.power_select", 10f, 1.5f);
    }
}
