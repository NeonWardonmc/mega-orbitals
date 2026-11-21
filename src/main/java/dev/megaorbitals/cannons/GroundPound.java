package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

/** A colossal invisible fist smashes everyone into the ground, leaving them dazed in their own crater. */
public final class GroundPound extends Cannon {
    public GroundPound() {
        super("&6&lGround Pound", Material.HEAVY_CORE, "A colossal invisible fist smashes everyone", "into the ground, leaving them dazed.");
        warning(30, "#C08040");
        option("radius", 25, "Range.");
        option("damage", 12.0, "Damage to each target.");
        option("daze-seconds", 6, "How long targets are dazed.");
        option("crater-power", 2.5, "Power of the crater under each target.");
    }

    @Override
    public void strike(Strike s) {
        for (LivingEntity le : s.victims(s.at(), d("radius"))) {
            le.setVelocity(new Vector(0, -4, 0));
            s.later(3, () -> {
                if (!le.isValid()) return;
                s.explode(le.getLocation().add(0, -0.5, 0), (float) d("crater-power"), false);
                s.damage(le, d("damage"));
                le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, i("daze-seconds") * 20, 4));
                le.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, i("daze-seconds") * 20, 0));
                le.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, i("daze-seconds") * 20, 128));
                Fx.block(s.world, Particle.BLOCK, le.getLocation(), Material.DIRT, 40, 1);
            });
        }
        s.sound(s.at(), "item.mace.smash_ground_heavy", 10f, 0.5f);
        Fx.ring(s.world, Particle.EXPLOSION, s.at(0, 0.5, 0), 4, 12);
    }
}
