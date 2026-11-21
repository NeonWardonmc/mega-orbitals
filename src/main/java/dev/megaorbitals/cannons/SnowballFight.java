package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import java.util.*;

/** Drops a snow golem army and a freezing snowball barrage that knocks everyone around. */
public final class SnowballFight extends Cannon {
    public SnowballFight() {
        super("&f&lSnowball Fight", Material.SNOWBALL, "Drops a snow golem army plus a freezing", "snowball barrage that knocks everyone around.");
        warning(15, "#FFFFFF");
        noBlockDamage();
        option("golems", 8, "Snow golems (they attack mobs).");
        option("radius", 25, "Range of the barrage.");
        option("seconds", 10, "Length of the barrage.");
    }

    @Override
    public void strike(Strike s) {
        Random rnd = new Random();
        for (Location l : Fx.circle(s.at(), 4, i("golems"))) s.mob(s.surface(l), Snowman.class, null);
        s.every(3, i("seconds") * 20 / 3, t -> {
            for (LivingEntity le : s.victims(s.at(), d("radius"))) {
                if (le instanceof Snowman) continue;
                Location from = le.getLocation().add(rnd.nextGaussian() * 4, 10, rnd.nextGaussian() * 4);
                s.world.spawn(from, Snowball.class, sb -> sb.setVelocity(Fx.dir(from, le.getEyeLocation()).multiply(2)));
                le.setFreezeTicks(Math.min(le.getMaxFreezeTicks() + 40, le.getFreezeTicks() + 6));
            }
        });
        s.sound(s.at(), "entity.snow_golem.ambient", 8f, 1f);
    }
}
