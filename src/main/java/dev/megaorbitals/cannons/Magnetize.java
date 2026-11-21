package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;

/** Everyone in range is magnetised, dragged together into one tight clump, then slammed together. */
public final class Magnetize extends Cannon {
    public Magnetize() {
        super("&7&lMagnetize", Material.LODESTONE, "Magnetises everyone, drags them into one", "tight clump, then slams them together.");
        warning(25, "#8080A0");
        noBlockDamage();
        option("radius", 30, "Range.");
        option("seconds", 6, "How long they are pulled.");
        option("slam-damage", 10.0, "Damage when they collide.");
    }

    @Override
    public void strike(Strike s) {
        Location c = s.at(0, 1, 0);
        s.every(1, i("seconds") * 20, t -> {
            for (LivingEntity le : s.victims(c, d("radius"))) {
                le.setVelocity(Fx.dir(le.getLocation(), c).multiply(Math.min(1.2, le.getLocation().distance(c) * 0.15)));
                if (t % 4 == 0) Fx.dustLine(s.world, le.getLocation().add(0, 1, 0), c, Color.GRAY, 0.8f, 1.0);
            }
            if (t % 10 == 0) s.sound(c, "block.lodestone.place", 4f, 0.5f);
        });
        s.later(i("seconds") * 20L, () -> {
            for (LivingEntity le : s.victims(c, 6)) s.damage(le, d("slam-damage"));
            Fx.p(s.world, Particle.EXPLOSION_EMITTER, c, 1, 0, 0);
            s.sound(c, "block.anvil.land", 8f, 0.5f);
        });
    }
}
