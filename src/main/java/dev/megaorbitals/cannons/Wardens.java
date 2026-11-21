package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;

/** Wardens dig up out of the ground already furious at everyone nearby. */
public final class Wardens extends Cannon {
    public Wardens() {
        super("&3&lWarden Uprising", Material.SCULK_SHRIEKER, "Wardens dig out of the ground,", "already furious at everyone nearby.");
        warning(60, "#0A3040");
        option("wardens", 2, "Number of wardens.");
        option("anger-radius", 40, "Everyone in this range makes them angry.");
    }

    @Override public void warn(Strike s) {
        super.warn(s);
        s.every(15, warningTicks() / 15, t -> s.sound(s.at(), "entity.warden.heartbeat", 8f, 1f));
    }

    @Override
    public void strike(Strike s) {
        for (Location l : Fx.circle(s.at(), 3, i("wardens"))) {
            Warden w = s.mob(s.surface(l), Warden.class, null);
            for (LivingEntity le : s.victims(s.at(), d("anger-radius"))) {
                if (!(le instanceof Warden)) w.setAnger(le, 150);
            }
            s.hunt(w, d("anger-radius"));
        }
        s.sound(s.at(), "entity.warden.emerge", 10f, 1f);
    }
}
