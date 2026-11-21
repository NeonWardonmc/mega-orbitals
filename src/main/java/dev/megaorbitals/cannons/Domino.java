package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.util.Vector;

/** Explosions topple forward like dominoes in the direction you face, each one bigger than the last. */
public final class Domino extends Cannon {
    public Domino() {
        super("&e&lDomino Blast", Material.SMOOTH_QUARTZ, "Explosions topple forward like dominoes the way", "you face, each one bigger than the last.");
        warning(20, "#FFE060");
        option("length", 60, "How far the dominoes run.");
        option("spacing", 4.0, "Blocks between blasts.");
        option("start-power", 2.0, "Power of the first blast.");
        option("end-power", 7.0, "Power of the last blast.");
    }

    @Override
    public void strike(Strike s) {
        Vector dir = s.shooter != null ? s.shooter.getLocation().getDirection().setY(0) : new Vector(1, 0, 0);
        if (dir.lengthSquared() < 0.01) dir = new Vector(1, 0, 0);
        Vector step = dir.normalize().multiply(d("spacing"));
        int n = (int) (d("length") / d("spacing"));
        s.every(3, n, t -> {
            Location l = s.surface(s.at().add(step.clone().multiply(t)));
            float p = (float) (d("start-power") + (d("end-power") - d("start-power")) * t / Math.max(1, n - 1));
            s.explode(l, p, false);
        });
    }
}
