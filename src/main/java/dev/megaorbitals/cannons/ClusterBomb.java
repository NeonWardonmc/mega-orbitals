package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.util.Vector;
import java.util.function.Consumer;

/** One bomb that splits into 8, which each split again into 4 more. */
public final class ClusterBomb extends Cannon {
    public ClusterBomb() {
        super("&6&lCluster Bomb", Material.GUNPOWDER, "One bomb splits into eight,", "and each of those splits again.");
        warning(20, "#FFA020");
        option("height", 45, "Drop height.");
        option("first-split", 8, "Bomblets from the first split.");
        option("second-split", 4, "Bomblets from each second split.");
        option("power", 3.5, "Power of the final bomblets.");
    }

    @Override protected double warningRadius() { return 20; }

    @Override
    public void strike(Strike s) {
        TNTPrimed main = s.tnt(s.at(0, i("height"), 0), 6f, 400);
        s.later(25, () -> {
            if (!main.isValid()) return;
            Location c = main.getLocation();
            main.remove();
            split(s, c, i("first-split"), 0.7, 400, b -> s.later(18, () -> {
                if (!b.isValid()) return;
                Location c2 = b.getLocation();
                b.remove();
                split(s, c2, i("second-split"), 0.45, -1, null);
            }));
        });
    }

    private void split(Strike s, Location c, int n, double speed, int fuse, Consumer<TNTPrimed> after) {
        Fx.p(s.world, Particle.EXPLOSION, c, 3, 0.5, 0);
        s.sound(c, "entity.firework_rocket.blast", 6f, 0.6f);
        int realFuse = fuse > 0 ? fuse : Strike.fallTicks(c.getY() - s.at().getY()) + 5;
        for (int k = 0; k < n; k++) {
            double a = 2 * Math.PI * k / n;
            TNTPrimed t = s.tnt(c, (float) d("power"), realFuse);
            t.setVelocity(new Vector(Math.cos(a) * speed, 0.2, Math.sin(a) * speed));
            if (after != null) after.accept(t);
        }
    }
}
