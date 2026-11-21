package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;

/** Sustained artillery fire: shells whistle in and land randomly all over the area. */
public final class Bombardment extends Cannon {
    public Bombardment() {
        super("&6&lArtillery Bombardment", Material.BLAST_FURNACE, "Sustained artillery fire. Shells whistle in", "and land randomly across the area.");
        warning(30, "#FF9000");
        option("radius", 35, "Area covered by shells.");
        option("seconds", 30, "How long the bombardment lasts.");
        option("shells-per-second", 2, "Shells each second.");
        option("power", 3.5, "Power of each shell.");
    }

    @Override
    public void strike(Strike s) {
        int every = Math.max(1, 20 / Math.max(1, i("shells-per-second")));
        Color orange = Color.fromRGB(255, 140, 0);
        s.every(every, i("seconds") * 20 / every, t -> {
            Location l = s.randomPoint(d("radius"));
            s.sound(l, "entity.firework_rocket.launch", 6f, 0.5f);
            s.every(2, 10, k -> {
                Fx.dustRing(s.world, l.clone().add(0, 0.2, 0), 2.5 * (1 - k / 10.0) + 0.3, orange, 1.2f);
                if (k == 9) s.explode(l, (float) d("power"), false);
            });
        });
    }
}
