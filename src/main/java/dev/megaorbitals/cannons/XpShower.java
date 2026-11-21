package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import java.util.*;

/** Support: a shower of experience orbs rains down for everyone to grab. */
public final class XpShower extends Cannon {
    public XpShower() {
        super("&a&lXP Shower", Material.EXPERIENCE_BOTTLE, "Support: a shower of experience orbs", "rains down for everyone to grab.");
        warning(5, "#80FF40");
        noBlockDamage();
        option("total-xp", 2000, "Total experience dropped.");
        option("orbs", 100, "Number of orbs.");
        option("radius", 8, "Spread.");
    }

    @Override
    public void strike(Strike s) {
        Random rnd = new Random();
        int per = Math.max(1, i("total-xp") / Math.max(1, i("orbs")));
        s.every(1, i("orbs"), t -> {
            Location l = s.at(rnd.nextGaussian() * d("radius") / 2, 12, rnd.nextGaussian() * d("radius") / 2);
            s.world.spawn(l, ExperienceOrb.class, o -> o.setExperience(per));
            if (t % 5 == 0) s.sound(s.at(), "entity.experience_orb.pickup", 4f, 0.5f + rnd.nextFloat());
        });
    }
}
