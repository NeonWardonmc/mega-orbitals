package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;

/** A giant lens focuses sunlight onto the target. Devastating at noon, weak at night. Sand turns to glass. */
public final class SunStrike extends Cannon {
    public SunStrike() {
        super("&6&lSun Lens", Material.SPYGLASS, "A giant lens focuses sunlight onto the target.", "Brutal at noon, weak at night. Sand turns to glass.");
        warning(20, "#FFC020");
        option("radius", 5.0, "Size of the focused spot.");
        option("seconds", 8, "How long it burns.");
        option("day-damage", 10.0, "Damage every half second during the day.");
        option("night-damage", 1.0, "Damage every half second at night.");
    }

    @Override
    public void strike(Strike s) {
        long time = s.world.getTime();
        boolean day = time < 12300 || time > 23850;
        double dmg = day ? d("day-damage") : d("night-damage");
        double r = d("radius");
        Color sun = Color.fromRGB(255, 220, 80);
        if (s.shooter != null) s.actionBar(s.shooter, day ? "&6☀ Full sunlight. Maximum power." : "&7☾ Night time. The lens is weak.");
        s.every(2, i("seconds") * 10, t -> {
            Fx.dustLine(s.world, s.at(0, 80, 0), s.at(), sun, day ? 4f : 1f, 1);
            Fx.dustRing(s.world, s.at(0, 0.2, 0), r, sun, 2f);
            Fx.p(s.world, Particle.FLAME, s.at(), day ? 15 : 2, r / 2, 0.02);
            if (t % 5 != 0) return;
            for (LivingEntity le : s.victims(s.at(), r + 1)) { s.damage(le, dmg); if (day) le.setFireTicks(80); }
            if (day && s.breaks()) {
                for (int k = 0; k < 6; k++) {
                    Block b = s.randomPoint(r).add(0, -1, 0).getBlock();
                    Material m = b.getType();
                    if (m == Material.SAND || m == Material.RED_SAND) Blocks.set(b, Material.GLASS);
                    else if (m.isSolid() && Math.random() < 0.3) Blocks.set(b, Material.MAGMA_BLOCK);
                    Block up = b.getRelative(0, 1, 0);
                    if (up.getType().isAir() && Math.random() < 0.2) up.setType(Material.FIRE);
                }
            }
            s.sound(s.at(), "block.fire.ambient", 4f, 0.6f);
        });
    }
}
