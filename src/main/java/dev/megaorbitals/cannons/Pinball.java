package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;
import java.util.*;

/** Everyone nearby turns into a pinball, bouncing off in random directions. */
public final class Pinball extends Cannon {
    public Pinball() {
        super("&d&lPinball", Material.SLIME_BALL, "Everyone nearby turns into a pinball,", "bouncing off in random directions.");
        warning(15, "#FF60FF");
        noBlockDamage();
        option("radius", 25, "Range.");
        option("seconds", 10, "How long the bouncing lasts.");
        option("bounce", 1.4, "Bounce strength.");
    }

    @Override
    public void strike(Strike s) {
        Random rnd = new Random();
        s.every(8, i("seconds") * 20 / 8, t -> {
            for (LivingEntity le : s.victims(s.at(), d("radius"))) {
                double a = rnd.nextDouble() * Math.PI * 2;
                le.setVelocity(new Vector(Math.cos(a), 0, Math.sin(a)).multiply(d("bounce")).setY(0.5));
                le.setFallDistance(0);
                s.sound(le.getLocation(), "block.note_block.bell", 1.5f, 0.5f + rnd.nextFloat());
                Fx.p(s.world, Particle.NOTE, le.getLocation().add(0, 2, 0), 1, 0, 1);
            }
        });
    }
}
