package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;
import java.util.*;

/** Raises a shimmering dome that turns every arrow, trident and fireball around and sends it back at whoever shot it. */
public final class Deflector extends Cannon {
    public Deflector() {
        super("&b&lProjectile Deflector", Material.SHIELD, "A dome that sends every arrow, trident and", "fireball straight back at whoever shot it.");
        warning(15, "#80E0FF");
        noBlockDamage();
        option("radius", 10.0, "Size of the dome.");
        option("seconds", 45, "How long the dome lasts.");
        option("speed-boost", 1.2, "Returned projectiles fly this much faster.");
        option("deflect-own", false, "Also deflect projectiles fired by the shooter.");
    }

    @Override
    public void strike(Strike s) {
        double r = d("radius");
        Location c = s.at();
        Set<UUID> done = new HashSet<>();
        Color col = Color.fromRGB(0x80E0FF);
        s.sound(c, "block.beacon.power_select", 4f, 1.4f);
        s.every(1, i("seconds") * 20, t -> {
            if (t % 5 == 0) Fx.dustSphere(s.world, c, r, col, 1.1f, (int) (r * 12));
            for (Entity e : s.world.getNearbyEntities(c, r + 2, r + 2, r + 2)) {
                if (!(e instanceof Projectile p) || done.contains(p.getUniqueId())) continue;
                if (p.getLocation().distance(c) > r) continue;
                if (p instanceof AbstractArrow a && a.isInBlock()) continue;
                ProjectileSource src = p.getShooter();
                if (!(src instanceof LivingEntity owner) || !owner.isValid()) continue;
                if (!b("deflect-own") && s.shooter != null && owner.getUniqueId().equals(s.shooter.getUniqueId())) continue;
                done.add(p.getUniqueId());
                double speed = Math.max(0.8, p.getVelocity().length()) * d("speed-boost");
                Vector back = Fx.dir(p.getLocation(), owner.getEyeLocation().subtract(0, 0.4, 0)).multiply(speed);
                if (s.shooter != null) p.setShooter(s.shooter);
                else p.setShooter(null);
                if (p instanceof Fireball f) {
                    f.setVelocity(back);
                    f.setAcceleration(back.clone().normalize().multiply(0.1));
                } else {
                    p.setVelocity(back);
                }
                Fx.p(s.world, Particle.END_ROD, p.getLocation(), 8, 0.2, 0.05);
                s.sound(p.getLocation(), "item.shield.block", 2f, 1.4f);
            }
            if (t % 40 == 0) s.sound(c, "block.beacon.ambient", 2f, 1.5f);
        });
    }
}
