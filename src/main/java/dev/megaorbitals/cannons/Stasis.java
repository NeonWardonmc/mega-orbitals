package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import java.util.*;

/** Freezes time: everyone nearby is locked in place and can't move at all. */
public final class Stasis extends Cannon {
    public Stasis() {
        super("&b&lStasis Field", Material.CLOCK, "Freezes time. Everyone nearby is locked", "in place and can't move an inch.");
        warning(20, "#80FFFF");
        noBlockDamage();
        option("radius", 20, "Range.");
        option("seconds", 10, "How long time is frozen.");
    }

    @Override
    public void strike(Strike s) {
        Map<LivingEntity, Location> frozen = new HashMap<>();
        for (LivingEntity le : s.victims(s.at(), d("radius"))) {
            frozen.put(le, le.getLocation());
            if (le instanceof Mob m) m.setAI(false);
            if (le instanceof Player p) Fx.title(p, "&b&lFROZEN IN TIME", "", 40);
        }
        int ticks = i("seconds") * 20;
        s.every(1, ticks, t -> {
            Fx.dustSphere(s.world, s.at(0, 1, 0), d("radius"), Color.fromRGB(120, 255, 255), 1f, 150);
            frozen.forEach((le, l) -> {
                if (!le.isValid()) return;
                Location to = l.clone();
                to.setYaw(le.getLocation().getYaw());
                to.setPitch(le.getLocation().getPitch());
                if (le.getLocation().distanceSquared(l) > 0.01) le.teleport(to);
                le.setVelocity(new org.bukkit.util.Vector());
                le.setFallDistance(0);
            });
            if (t % 20 == 0) s.sound(s.at(), "block.note_block.chime", 4f, 0.5f);
        });
        s.later(ticks, () -> frozen.keySet().forEach(le -> { if (le instanceof Mob m && le.isValid()) m.setAI(true); }));
    }
}
