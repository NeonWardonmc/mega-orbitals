package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;

/** A warden scream from orbit that hits every living thing nearby, straight through walls. */
public final class SonicBoom extends Cannon {
    public SonicBoom() {
        super("&3&lSonic Boom", Material.ECHO_SHARD, "A warden's scream from orbit.", "Hits everything nearby, through walls.");
        warning(40, "#1080A0");
        option("radius", 30, "Range of the scream.");
        option("damage", 30.0, "Damage to each target.");
        option("knockback", 3.0, "How far targets are thrown.");
        option("screams", 3, "Number of screams.");
    }

    @Override
    public void strike(Strike s) {
        Location c = s.at(0, 2, 0);
        s.every(30, i("screams"), t -> {
            s.sound(c, "entity.warden.sonic_boom", 10f, 0.8f);
            for (LivingEntity le : s.victims(c, d("radius"))) {
                Location to = le.getLocation().add(0, 1, 0);
                Fx.line(s.world, Particle.SONIC_BOOM, c, to, 1.5);
                s.damage(le, d("damage"));
                le.setVelocity(Fx.dir(c, to).multiply(d("knockback")).setY(0.8));
            }
            Fx.ring(s.world, Particle.SONIC_BOOM, c, 3, 12);
        });
    }
}
