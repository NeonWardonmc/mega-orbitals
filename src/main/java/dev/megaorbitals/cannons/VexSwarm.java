package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;

/** A screaming cloud of vexes that fly through walls. They fade after a while. */
public final class VexSwarm extends Cannon {
    public VexSwarm() {
        super("&7&lVex Cloud", Material.IRON_SWORD, "A screaming cloud of vexes that fly", "straight through walls.");
        warning(20, "#B0C0D0");
        noBlockDamage();
        option("vexes", 16, "Number of vexes.");
        option("lifetime-seconds", 40, "Vexes fade after this long.");
    }

    @Override
    public void strike(Strike s) {
        for (Location l : Fx.circle(s.at(0, 6, 0), 5, i("vexes"))) {
            Vex v = s.mob(l, Vex.class, vx -> vx.setLimitedLifetimeTicks(i("lifetime-seconds") * 20));
            s.hunt(v, 50);
        }
        s.sound(s.at(), "entity.vex.charge", 10f, 0.6f);
    }
}
