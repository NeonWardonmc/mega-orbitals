package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;

/** A hurricane-force blast of wind pours from you wherever you look, blowing everything away. */
public final class LeafBlower extends Cannon {
    public LeafBlower() {
        super("&f&lGale Cannon", Material.WIND_CHARGE, "Hurricane-force wind pours from you wherever", "you look, blowing everything away.");
        warning(0, "#E0F0FF");
        noBlockDamage();
        option("seconds", 6, "How long it blows.");
        option("range", 20.0, "Wind range.");
        option("cone", 0.4, "Cone width.");
        option("strength", 1.4, "Push strength.");
    }

    @Override
    public void strike(Strike s) {
        Player me = s.shooter;
        if (me == null) return;
        double range = d("range"), cone = d("cone");
        s.every(1, i("seconds") * 20, t -> {
            if (!me.isOnline() || me.isDead()) return;
            Location eye = me.getEyeLocation();
            Vector look = eye.getDirection();
            for (int k = 0; k < 6; k++) {
                Vector v = look.clone().add(new Vector(Math.random() - 0.5, Math.random() - 0.5, Math.random() - 0.5).multiply(cone)).normalize();
                me.getWorld().spawnParticle(Particle.CLOUD, eye.clone().add(look), 0, v.getX(), v.getY(), v.getZ(), 1.2, null, true);
            }
            for (Entity e : me.getWorld().getNearbyEntities(eye, range, range, range)) {
                if (e.equals(me) || (e instanceof LivingEntity && s.spared(e))) continue;
                Vector to = e.getLocation().toVector().subtract(eye.toVector());
                if (to.length() > range || to.clone().normalize().dot(look) < 1 - cone) continue;
                e.setVelocity(look.clone().multiply(d("strength") * (1.2 - to.length() / range)).setY(0.3));
            }
            if (t % 10 == 0) me.getWorld().playSound(eye, "entity.breeze.wind_burst", 1f, 0.8f);
        });
    }
}
