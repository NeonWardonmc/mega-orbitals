package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;
import java.util.*;

/** Every explosion spawns more bombs, which spawn more bombs... a runaway chain reaction. */
public final class ChainReaction extends Cannon {
    public ChainReaction() {
        super("&c&lChain Reaction", Material.TNT, "Every explosion spawns more bombs, which spawn", "more bombs... a runaway chain reaction.");
        warning(25, "#FF4020");
        option("generations", 4, "How many times the chain multiplies.");
        option("children", 3, "Bombs spawned by each explosion.");
        option("power", 3.0, "Power of each bomb.");
    }

    @Override
    public void strike(Strike s) {
        bomb(s, s.at(0, 1, 0), new Vector(0, 0.5, 0), 0);
    }

    private void bomb(Strike s, Location at, Vector v, int gen) {
        TNTPrimed t = s.tnt(at, (float) d("power"), 30 + new Random().nextInt(15));
        t.setVelocity(v);
        Location[] last = {at};
        s.loop(1, k -> {
            if (t.isValid()) { last[0] = t.getLocation(); return true; }
            if (gen + 1 < i("generations")) {
                Random r = new Random();
                for (int c = 0; c < i("children"); c++) {
                    double a = r.nextDouble() * Math.PI * 2;
                    bomb(s, last[0].clone().add(0, 0.5, 0), new Vector(Math.cos(a) * 0.6, 0.6, Math.sin(a) * 0.6), gen + 1);
                }
            }
            return false;
        });
    }
}
