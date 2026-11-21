package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;
import java.util.*;

/** Traps every target in a floating bubble that drifts up into the sky... and pops. */
public final class Bubble extends Cannon {
    public Bubble() {
        super("&b&lBubble Trap", Material.GLASS, "Traps every target in a floating bubble that", "drifts up into the sky... and pops.");
        warning(15, "#A0E0FF");
        noBlockDamage();
        option("radius", 20, "Range.");
        option("seconds", 6, "How long they float up.");
        option("rise-speed", 0.35, "How fast the bubbles rise.");
    }

    @Override
    public void strike(Strike s) {
        Map<LivingEntity, Location> bubbles = new HashMap<>();
        for (LivingEntity le : s.victims(s.at(), d("radius"))) bubbles.put(le, le.getLocation().add(0, 1, 0));
        Color c = Color.fromRGB(170, 230, 255);
        int ticks = i("seconds") * 20;
        s.every(1, ticks, t -> bubbles.forEach((le, centre) -> {
            if (!le.isValid()) return;
            centre.add(0, d("rise-speed"), 0);
            Vector toCentre = centre.toVector().subtract(le.getLocation().add(0, 1, 0).toVector());
            le.setVelocity(toCentre.multiply(0.5));
            le.setFallDistance(0);
            if (t % 3 == 0) Fx.dustSphere(s.world, centre, 1.6, c, 0.8f, 40);
        }));
        s.later(ticks, () -> bubbles.forEach((le, centre) -> {
            Fx.p(s.world, Particle.SPLASH, centre, 40, 0.8, 0.1);
            s.sound(centre, "block.bubble_column.bubble_pop", 3f, 0.6f);
        }));
        s.sound(s.at(), "block.bubble_column.upwards_inside", 6f, 1f);
    }
}
