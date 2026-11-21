package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;

/** A no-go zone: a glowing barrier that throws out anyone who tries to get in. */
public final class Repulsor extends Cannon {
    public Repulsor() {
        super("&b&lRepulsor Field", Material.SHIELD, "A glowing no-go zone. Anyone who", "tries to get in is thrown back out.");
        warning(15, "#40C0FF");
        noBlockDamage();
        option("radius", 14.0, "Size of the zone.");
        option("seconds", 45, "How long the zone lasts.");
        option("push", 1.6, "How hard intruders are thrown.");
        option("shooter-allowed", true, "The shooter can stand inside safely.");
    }

    @Override
    public void strike(Strike s) {
        Location c = s.at();
        double r = d("radius");
        Color blue = Color.fromRGB(80, 190, 255);
        s.every(2, i("seconds") * 10, t -> {
            Fx.dustRing(s.world, c.clone().add(0, 0.3 + (t % 10) * 0.3, 0), r, blue, 1.2f);
            for (LivingEntity le : s.victims(c, r)) {
                if (b("shooter-allowed") && s.shooter != null && le.equals(s.shooter)) continue;
                le.setVelocity(Fx.dir(c, le.getLocation()).setY(0).multiply(d("push")).setY(0.5));
                s.sound(le.getLocation(), "block.beacon.deactivate", 1f, 2f);
            }
        });
    }
}
