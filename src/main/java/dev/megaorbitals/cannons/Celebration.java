package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.util.Vector;
import java.util.*;

/** A harmless fireworks show high in the sky. Nobody gets hurt. */
public final class Celebration extends Cannon {
    public Celebration() {
        super("&d&lCelebration", Material.FIREWORK_ROCKET, "A harmless fireworks show high in the sky.", "Nobody gets hurt.");
        warning(5, "#FF80FF");
        noBlockDamage();
        option("seconds", 20, "Length of the show.");
        option("rockets-per-second", 4, "Rockets each second.");
    }

    @Override
    public void strike(Strike s) {
        Random rnd = new Random();
        Color[] c = {Color.RED, Color.ORANGE, Color.YELLOW, Color.LIME, Color.AQUA, Color.BLUE, Color.PURPLE, Color.FUCHSIA, Color.WHITE};
        FireworkEffect.Type[] types = FireworkEffect.Type.values();
        int every = Math.max(1, 20 / Math.max(1, i("rockets-per-second")));
        s.every(every, i("seconds") * 20 / every, t -> {
            Location l = s.at(rnd.nextGaussian() * 8, 1, rnd.nextGaussian() * 8);
            s.world.spawn(l, Firework.class, f -> {
                FireworkMeta m = f.getFireworkMeta();
                m.setPower(2 + rnd.nextInt(2));
                m.addEffect(FireworkEffect.builder().with(types[rnd.nextInt(types.length)]).withColor(c[rnd.nextInt(c.length)])
                        .withFade(c[rnd.nextInt(c.length)]).trail(true).flicker(rnd.nextBoolean()).build());
                f.setFireworkMeta(m);
                f.setVelocity(new Vector(rnd.nextGaussian() * 0.05, 0.6, rnd.nextGaussian() * 0.05));
            });
        });
    }
}
