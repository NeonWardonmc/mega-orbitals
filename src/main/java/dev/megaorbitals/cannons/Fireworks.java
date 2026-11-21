package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.meta.FireworkMeta;
import java.util.*;

/** A deadly firework finale: rockets burst right in everyone's face. */
public final class Fireworks extends Cannon {
    public Fireworks() {
        super("&d&lGrand Finale", Material.FIREWORK_STAR, "A deadly firework show. Rockets burst", "right in everyone's face.");
        warning(20, "#FF60C0");
        noBlockDamage();
        option("radius", 30, "Range.");
        option("seconds", 10, "Length of the show.");
        option("stars", 6, "Star effects per rocket (more = more damage).");
    }

    private static final Color[] COLORS = {Color.RED, Color.ORANGE, Color.YELLOW, Color.LIME, Color.AQUA, Color.BLUE, Color.PURPLE, Color.FUCHSIA, Color.WHITE};
    private static final FireworkEffect.Type[] TYPES = FireworkEffect.Type.values();

    @Override
    public void strike(Strike s) {
        Random rnd = new Random();
        s.every(5, i("seconds") * 4, t -> {
            List<LivingEntity> targets = s.victims(s.at(), d("radius"));
            Location l = targets.isEmpty() ? s.randomPoint(d("radius")).add(0, 3, 0)
                    : targets.get(rnd.nextInt(targets.size())).getLocation().add(rnd.nextGaussian(), 1.5, rnd.nextGaussian());
            Firework fw = s.world.spawn(l, Firework.class, f -> {
                FireworkMeta m = f.getFireworkMeta();
                for (int k = 0; k < i("stars"); k++) {
                    m.addEffect(FireworkEffect.builder().with(TYPES[rnd.nextInt(TYPES.length)])
                            .withColor(COLORS[rnd.nextInt(COLORS.length)]).withFade(COLORS[rnd.nextInt(COLORS.length)])
                            .flicker(rnd.nextBoolean()).trail(true).build());
                }
                f.setFireworkMeta(m);
                if (s.shooter != null) f.setShooter(s.shooter);
            });
            s.tag(fw);
            s.later(1, fw::detonate);
        });
    }
}
