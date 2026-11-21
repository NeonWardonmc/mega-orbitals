package com.example.orbitaladdon;

import dev.megaorbitals.Cannon;
import dev.megaorbitals.Fx;
import dev.megaorbitals.Strike;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import java.util.concurrent.ThreadLocalRandom;

/** A harmless party cannon: bursts of colourful confetti all over the target. Shows the basics of a cannon. */
public final class ConfettiCannon extends Cannon {

    public ConfettiCannon() {
        // Name (& colour codes), item icon, then the description lines.
        super("&d&lConfetti Cannon", Material.PINK_DYE, "Covers the target in bursts", "of colourful confetti.");
        warning(20, "#FF80FF");          // 1 second warning with a pink targeting ring
        noBlockDamage();                  // never changes blocks
        catalog("common", "fun");         // default tier and menu tab
        // Settings show up under "settings:" in plugins/MegaOrbitals/configs/confetticannon.yml
        option("bursts", 20, "How many confetti bursts.");
        option("radius", 8.0, "How far the confetti spreads.");
    }

    @Override
    public void strike(Strike s) {
        // s.every(period, times, tick -> ...) repeats on the server thread and stops by itself.
        s.every(2, i("bursts"), t -> {
            Location l = s.randomPoint(d("radius")).add(0, 1.5, 0);
            Color c = Color.fromRGB(ThreadLocalRandom.current().nextInt(0xFFFFFF));
            Fx.dust(s.world, l, c, 1.8f, 25, 0.6);
            Fx.p(s.world, Particle.FIREWORK, l, 10, 0.2, 0.1);
            s.sound(l, "entity.firework_rocket.twinkle", 1.5f, 1.2f);
        });
        for (Player p : s.world.getPlayers()) {
            if (p.getLocation().distance(s.at()) < 30) Fx.title(p, "&d&lPARTY!", "&7courtesy of " + (s.shooter == null ? "someone" : s.shooter.getName()), 40);
        }
    }
}
