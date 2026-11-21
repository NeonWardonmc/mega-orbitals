package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;

/** A drilling beam that blasts straight down to bedrock. */
public final class Drill extends Cannon {
    public Drill() {
        super("&8&lOrbital Drill", Material.NETHERITE_PICKAXE, "A beam that blasts a shaft", "straight down to bedrock.");
        warning(30, "#FFFFFF");
        option("depth", 200, "How many blocks down it drills.");
        option("power", 5.0, "Power of each blast.");
        option("blocks-per-blast", 3, "Blocks between each blast.");
    }

    @Override
    public void strike(Strike s) {
        Location start = s.at();
        int step = Math.max(1, i("blocks-per-blast"));
        int depth = i("depth");
        float power = (float) d("power");
        s.loop(2, t -> {
            Location l = start.clone().subtract(0, t * step, 0);
            if (t * step > depth || l.getY() <= s.world.getMinHeight() + 1) return false;
            Fx.line(s.world, Particle.END_ROD, l, l.clone().add(0, 40, 0), 1.0);
            s.explode(l, power, false);
            return true;
        });
    }
}
