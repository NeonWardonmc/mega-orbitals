package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import java.util.*;

/** A ring of bombs orbits high above the target, spirals inward, and collapses into one huge blast. */
public final class HaloCollapse extends Cannon {
    public HaloCollapse() {
        super("&c&lHalo Collapse", Material.GOLD_BLOCK, "A ring of bombs orbits above the target,", "spirals inward and collapses into one blast.");
        warning(20, "#FFB020");
        option("bombs", 24, "Bombs in the ring.");
        option("radius", 22.0, "Starting ring size.");
        option("seconds", 6, "Time to collapse.");
        option("ring-power", 3.0, "Power of each bomb as it lands.");
        option("final-power", 10.0, "Power of the final blast.");
    }

    @Override
    public void strike(Strike s) {
        int n = i("bombs"), ticks = i("seconds") * 20;
        List<BlockDisplay> ring = new ArrayList<>();
        for (int k = 0; k < n; k++) ring.add(Fx.display(s.at(0, 15, 0), Material.TNT, 1.2f));
        s.every(1, ticks + 1, t -> {
            double p = (double) t / ticks;
            double r = d("radius") * (1 - p);
            double y = 15 * (1 - p) + 1;
            for (int k = 0; k < n; k++) {
                double a = 2 * Math.PI * k / n + p * Math.PI * 4;
                Location l = s.at(Math.cos(a) * r, y, Math.sin(a) * r);
                ring.get(k).teleport(l);
                if (t % 3 == 0) Fx.p(s.world, Particle.FLAME, l, 1, 0.1, 0.01);
            }
            if (t % 10 == 0) s.sound(s.at(), "entity.tnt.primed", 4f, 0.5f + (float) p);
            if (t < ticks) return;
            for (int k = 0; k < n; k++) {
                Location l = ring.get(k).getLocation();
                ring.get(k).remove();
                s.explode(l, (float) d("ring-power"), false);
            }
            s.later(4, () -> s.explode(s.at(), (float) d("final-power"), false));
        });
    }
}
