package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;

/** Forces the sun to rise: it becomes morning and every undead mob nearby bursts into flames. */
public final class Dawn extends Cannon {
    public Dawn() {
        super("&e&lForced Dawn", Material.SUNFLOWER, "Forces the sun to rise: it becomes morning and", "every undead mob nearby bursts into flames.");
        warning(10, "#FFE080");
        noBlockDamage();
        option("change-time", true, "Set the world time to morning.");
        option("radius", 60, "Range for burning undead.");
        option("damage", 10.0, "Holy damage to undead.");
    }

    @Override
    public void strike(Strike s) {
        if (b("change-time")) s.world.setTime(0);
        double r = d("radius");
        for (Entity e : s.world.getNearbyEntities(s.at(), r, r, r)) {
            if (!(e instanceof Zombie || e instanceof AbstractSkeleton || e instanceof Phantom || e instanceof Wither || e instanceof Zoglin)) continue;
            LivingEntity le = (LivingEntity) e;
            le.setFireTicks(400);
            s.damage(le, d("damage"));
            Fx.p(s.world, Particle.END_ROD, le.getLocation().add(0, 1, 0), 10, 0.3, 0.05);
        }
        Fx.dustLine(s.world, s.at(0, 100, 0), s.at(), Color.fromRGB(255, 230, 120), 4f, 1);
        s.soundWorld("block.beacon.activate", 1f, 1.5f);
    }
}
