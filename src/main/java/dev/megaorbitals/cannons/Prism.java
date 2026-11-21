package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import java.util.*;

/** A crystal prism appears above the target and splits a beam onto everyone nearby. */
public final class Prism extends Cannon {
    public Prism() {
        super("&d&lPrism", Material.AMETHYST_CLUSTER, "A prism in the sky splits a beam", "into rainbow lasers that hunt targets.");
        warning(30, "#FF60FF");
        option("radius", 30, "Range of the split beams.");
        option("beams", 8, "Maximum targets at once.");
        option("damage", 4.0, "Damage every half second.");
        option("seconds", 10, "How long the prism lasts.");
    }

    @Override
    public void strike(Strike s) {
        Location prism = s.at(0, 12, 0);
        BlockDisplay crystal = Fx.display(prism, Material.AMETHYST_BLOCK, 2f);
        Color[] rainbow = {Color.RED, Color.ORANGE, Color.YELLOW, Color.LIME, Color.AQUA, Color.BLUE, Color.PURPLE, Color.FUCHSIA};
        int steps = i("seconds") * 10;
        s.every(2, steps, t -> {
            Fx.dustLine(s.world, prism.clone().add(0, 80, 0), prism, Color.WHITE, 2f, 1.0);
            List<LivingEntity> targets = s.victims(prism, d("radius"));
            targets.sort(Comparator.comparingDouble(e -> e.getLocation().distanceSquared(prism)));
            for (int k = 0; k < Math.min(i("beams"), targets.size()); k++) {
                LivingEntity le = targets.get(k);
                Fx.dustLine(s.world, prism, le.getLocation().add(0, 1, 0), rainbow[k % rainbow.length], 1.5f, 0.5);
                if (t % 5 == 0) s.damage(le, d("damage"));
            }
            if (t % 10 == 0) s.sound(prism, "block.amethyst_block.resonate", 5f, 1.5f);
            if (t == steps - 1) {
                crystal.remove();
                s.sound(prism, "block.glass.break", 6f, 0.5f);
            }
        });
    }
}
