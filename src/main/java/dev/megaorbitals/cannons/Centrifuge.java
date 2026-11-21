package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

/** Spins everyone around the target faster and faster, then lets go and they fly off. */
public final class Centrifuge extends Cannon {
    public Centrifuge() {
        super("&d&lCentrifuge", Material.LODESTONE, "Spins everyone around the target faster", "and faster... then lets go.");
        warning(15, "#C080FF");
        noBlockDamage();
        option("radius", 18, "Range.");
        option("seconds", 5, "Spin time.");
        option("max-speed", 1.8, "Top speed.");
    }

    @Override
    public void strike(Strike s) {
        var caught = s.victims(s.at(), d("radius"));
        int ticks = i("seconds") * 20;
        s.every(1, ticks, t -> {
            double speed = d("max-speed") * (t + 1) / ticks;
            for (LivingEntity le : caught) {
                if (!le.isValid()) continue;
                Vector rel = le.getLocation().toVector().subtract(s.at().toVector()).setY(0);
                if (rel.lengthSquared() < 1) rel = new Vector(3, 0, 0);
                double dist = rel.length();
                Vector tangent = new Vector(-rel.getZ(), 0, rel.getX()).normalize().multiply(speed);
                Vector keep = rel.clone().normalize().multiply((6 - dist) * 0.1);
                le.setVelocity(tangent.add(keep).setY(0.08));
                le.setFallDistance(0);
                if (t % 20 == 0) le.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 100, 0, false, false));
            }
            if (t % 10 == 0) s.sound(s.at(), "item.elytra.flying", 4f, 0.5f + t / (float) ticks);
        });
        s.later(ticks, () -> {
            for (LivingEntity le : caught) if (le.isValid()) le.setVelocity(le.getVelocity().multiply(1.5).setY(0.8));
        });
    }
}
