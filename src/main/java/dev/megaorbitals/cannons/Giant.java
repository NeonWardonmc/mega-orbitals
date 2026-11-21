package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;

/** Drops one of Minecraft's forgotten Giants from orbit. The landing alone is devastating. */
public final class Giant extends Cannon {
    public Giant() {
        super("&2&lThe Giant", Material.ZOMBIE_HEAD, "Drops one of Minecraft's forgotten Giants", "from orbit. The landing alone is devastating.");
        warning(40, "#30A030");
        option("landing-damage", 25.0, "Damage to everything it lands near.");
        option("landing-radius", 7.0, "Size of the landing impact.");
        option("remove-after-seconds", 120, "Giant vanishes after this long (0 = stays).");
    }

    @Override
    public void strike(Strike s) {
        org.bukkit.entity.Giant g = s.mob(s.at(0, 40, 0), org.bukkit.entity.Giant.class, null);
        s.loop(1, t -> {
            if (!g.isValid() || t > 200) return false;
            if (!g.isOnGround() || t < 5) return true;
            Location l = g.getLocation();
            for (LivingEntity le : s.victims(l, d("landing-radius"))) {
                if (le.equals(g)) continue;
                s.damage(le, d("landing-damage"));
                le.setVelocity(Fx.dir(l, le.getLocation()).multiply(1.5).add(new Vector(0, 0.8, 0)));
            }
            Fx.p(s.world, Particle.EXPLOSION_EMITTER, l, 2, 2, 0);
            Fx.block(s.world, Particle.BLOCK, l, Material.DIRT, 100, 3);
            s.soundWorld("entity.generic.explode", 0.6f, 0.4f);
            return false;
        });
        if (i("remove-after-seconds") > 0) s.expire(g, i("remove-after-seconds") * 20L);
    }
}
