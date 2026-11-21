package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import java.util.*;

/** Buries hidden mines around the area. Anyone who walks over one gets blown up. */
public final class Minefield extends Cannon {
    public Minefield() {
        super("&7&lMinefield", Material.STONE_PRESSURE_PLATE, "Buries hidden mines all over the area.", "They stay armed for 5 minutes.");
        warning(20, "#606060");
        option("radius", 25, "Area the mines are spread over.");
        option("mines", 40, "Number of mines.");
        option("power", 4.0, "Explosion power of each mine.");
        option("armed-seconds", 300, "How long the mines stay armed.");
        option("trigger-distance", 1.5, "How close you must step to set one off.");
    }

    @Override
    public void strike(Strike s) {
        List<Location> mines = new ArrayList<>();
        for (int k = 0; k < i("mines"); k++) mines.add(s.randomPoint(d("radius")));
        for (Location m : mines) Fx.p(s.world, Particle.SMOKE, m, 10, 0.3, 0.01);
        s.sound(s.at(), "block.stone.place", 4f, 0.5f);
        double trig = d("trigger-distance");
        s.every(5, i("armed-seconds") * 4, t -> {
            Iterator<Location> it = mines.iterator();
            while (it.hasNext()) {
                Location m = it.next();
                if (t % 40 == 0) Fx.p(s.world, Particle.SMOKE, m.clone().add(0, 0.1, 0), 1, 0.1, 0);
                if (!s.victims(m, trig).isEmpty()) {
                    it.remove();
                    s.sound(m, "block.stone_pressure_plate.click_on", 2f, 1f);
                    s.later(4, () -> s.explode(m, (float) d("power"), false));
                }
            }
        });
    }
}
