package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

/** Crushing gravity: everyone in range is pinned to the ground and slowly crushed. */
public final class GravityWell extends Cannon {
    public GravityWell() {
        super("&8&lGravity Well", Material.ANVIL, "Crushing gravity pins everyone", "to the ground. No jumping. No flying.");
        warning(30, "#404060");
        option("radius", 25, "Range of the gravity well.");
        option("seconds", 15, "How long it lasts.");
        option("crush-damage", 2.0, "Damage every second.");
    }

    @Override
    public void strike(Strike s) {
        double r = d("radius");
        s.every(2, i("seconds") * 10, t -> {
            for (Location l : Fx.circle(s.at(0, 12 - (t % 12), 0), r, (int) r * 2)) Fx.block(s.world, Particle.FALLING_DUST, l, Material.GRAVEL, 1, 0.2);
            for (LivingEntity le : s.victims(s.at(), r)) {
                le.setVelocity(new Vector(le.getVelocity().getX() * 0.3, -3, le.getVelocity().getZ() * 0.3));
                le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 20, 5, false, false));
                if (le instanceof Player p) { p.setGliding(false); p.setFlying(false); }
                if (t % 10 == 0) s.damage(le, d("crush-damage"));
            }
            if (t % 20 == 0) s.sound(s.at(), "block.anvil.land", 4f, 0.5f);
        });
    }
}
