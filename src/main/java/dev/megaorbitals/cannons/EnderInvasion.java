package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import java.util.*;

/** Angry endermen invade, and everyone nearby keeps getting randomly teleported. */
public final class EnderInvasion extends Cannon {
    public EnderInvasion() {
        super("&5&lEnder Invasion", Material.ENDER_PEARL, "Angry endermen invade, and everyone", "nearby keeps randomly teleporting.");
        warning(30, "#A040FF");
        option("endermen", 10, "Number of endermen.");
        option("radius", 20, "Range.");
        option("seconds", 15, "How long the random teleports last.");
        option("teleport-range", 16, "How far each random teleport goes.");
    }

    @Override
    public void strike(Strike s) {
        Random rnd = new Random();
        for (int k = 0; k < i("endermen"); k++) {
            Enderman e = s.mob(s.randomPoint(d("radius")), Enderman.class, null);
            s.hunt(e, 40);
        }
        s.every(30, i("seconds") * 20 / 30, t -> {
            for (LivingEntity le : s.victims(s.at(), d("radius") * 1.5)) {
                if (le instanceof Enderman) continue;
                int tr = i("teleport-range");
                Location to = s.surface(le.getLocation().add(rnd.nextInt(tr * 2) - tr, 0, rnd.nextInt(tr * 2) - tr));
                to.setDirection(le.getLocation().getDirection());
                Fx.p(s.world, Particle.PORTAL, le.getLocation(), 40, 0.5, 0.5);
                le.teleport(to);
                s.sound(to, "entity.enderman.teleport", 2f, 1f);
            }
        });
    }
}
