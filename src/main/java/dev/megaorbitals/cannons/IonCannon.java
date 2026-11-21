package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;

/** Charges up with converging rings of ion energy, then detonates in three waves. */
public final class IonCannon extends Cannon {
    public IonCannon() {
        super("&9&lIon Cannon", Material.BEACON, "Charges with converging rings of energy,", "then detonates in three expanding waves.");
        warning(100, "#2080FF");
        option("radius", 28, "Size of the charge-up and outer wave.");
        option("core-power", 10.0, "Power of the central blast.");
        option("wave-power", 5.0, "Power of the wave blasts.");
        option("launch", 2.5, "How hard everyone is thrown upward.");
    }

    @Override
    public void warn(Strike s) {
        Color blue = Color.fromRGB(60, 140, 255);
        double r = d("radius");
        int steps = Math.max(1, warningTicks() / 2);
        s.sound(s.at(), "block.beacon.activate", 8f, 0.5f);
        s.every(2, steps, t -> {
            double p = t / (double) steps;
            for (int k = 0; k < 3; k++) Fx.dustRing(s.world, s.at(0, 0.3 + k * 4 * (1 - p), 0), r * (1 - p) + k, blue, 2f);
            Fx.dustLine(s.world, s.at(0, 100, 0), s.at(), Color.WHITE, 1.5f + (float) p * 2, 1.5);
            if (t % 5 == 0) s.sound(s.at(), "block.beacon.ambient", 6f, 0.5f + (float) p * 1.5f);
        });
    }

    @Override
    public void strike(Strike s) {
        Location c = s.at();
        double r = d("radius");
        s.explode(c, (float) d("core-power"), false);
        for (LivingEntity le : s.victims(c, r)) le.setVelocity(new Vector(0, d("launch"), 0));
        s.later(6, () -> { for (Location l : Fx.circle(c, r / 2, 12)) s.explode(s.surface(l), (float) d("wave-power"), false); });
        s.later(12, () -> { for (Location l : Fx.circle(c, r, 22)) s.explode(s.surface(l), (float) d("wave-power"), false); });
        s.soundWorld("entity.lightning_bolt.thunder", 1f, 0.5f);
    }
}
