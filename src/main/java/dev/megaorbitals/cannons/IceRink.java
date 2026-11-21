package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;
import java.util.*;

/** Turns the ground into slick blue ice and keeps shoving everyone so they slide out of control. */
public final class IceRink extends Cannon {
    public IceRink() {
        super("&b&lIce Rink", Material.BLUE_ICE, "Turns the ground to slick blue ice and keeps", "shoving everyone so they slide out of control.");
        warning(20, "#80D0FF");
        option("radius", 20, "Size of the rink.");
        option("seconds", 25, "How long the shoving lasts.");
        option("shove", 0.9, "Shove strength.");
    }

    @Override
    public void strike(Strike s) {
        Random rnd = new Random();
        int r = i("radius");
        s.blocks(Blocks.surface(s.at(), r, Material.BLUE_ICE));
        s.every(15, i("seconds") * 20 / 15, t -> {
            for (LivingEntity le : s.victims(s.at(), r)) {
                double a = rnd.nextDouble() * Math.PI * 2;
                le.setVelocity(le.getVelocity().add(new Vector(Math.cos(a), 0.1, Math.sin(a)).multiply(d("shove"))));
                Fx.p(s.world, Particle.SNOWFLAKE, le.getLocation(), 5, 0.3, 0.02);
            }
            s.sound(s.at(), "block.glass.step", 3f, 1.5f);
        });
    }
}
