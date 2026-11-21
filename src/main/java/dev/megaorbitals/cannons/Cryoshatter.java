package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/** Flash-freezes everyone solid... then shatters them. */
public final class Cryoshatter extends Cannon {
    public Cryoshatter() {
        super("&b&lCryoshatter", Material.BLUE_ICE, "Flash-freezes everyone solid...", "then shatters them.");
        warning(20, "#80E0FF");
        noBlockDamage();
        option("radius", 18, "Range.");
        option("freeze-seconds", 3, "How long they're frozen before shattering.");
        option("shatter-damage", 16.0, "Damage when the ice shatters.");
    }

    @Override
    public void strike(Strike s) {
        int ticks = i("freeze-seconds") * 20;
        var frozen = s.victims(s.at(), d("radius"));
        for (LivingEntity le : frozen) {
            le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, ticks, 254, false, false));
            le.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, ticks, 250, false, false));
            le.setFreezeTicks(le.getMaxFreezeTicks() + ticks);
        }
        s.every(2, ticks / 2, t -> {
            for (LivingEntity le : frozen) if (le.isValid()) Fx.block(s.world, Particle.BLOCK, le.getLocation().add(0, 1, 0), Material.ICE, 6, 0.4);
        });
        s.sound(s.at(), "block.glass.place", 8f, 0.5f);
        s.later(ticks, () -> {
            for (LivingEntity le : frozen) {
                if (!le.isValid()) continue;
                s.damage(le, d("shatter-damage"));
                Fx.block(s.world, Particle.BLOCK, le.getLocation().add(0, 1, 0), Material.PACKED_ICE, 60, 0.6);
                s.sound(le.getLocation(), "block.glass.break", 3f, 0.6f);
            }
        });
    }
}
