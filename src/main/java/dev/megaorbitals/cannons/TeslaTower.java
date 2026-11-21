package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;

/** Builds a copper Tesla tower that zaps the nearest target with lightning every second. */
public final class TeslaTower extends Cannon {
    public TeslaTower() {
        super("&6&lTesla Tower", Material.COPPER_BLOCK, "Builds a copper Tesla tower that zaps the", "nearest target with lightning every second.");
        warning(20, "#E08040");
        option("height", 7, "Tower height.");
        option("range", 22.0, "Zap range.");
        option("seconds", 60, "How long the tower is powered.");
        option("damage", 6.0, "Damage per zap.");
    }

    @Override
    public void strike(Strike s) {
        int h = i("height");
        if (s.breaks()) {
            for (int y = 0; y < h; y++) Blocks.set(s.at(0, y, 0).getBlock(), Material.COPPER_BLOCK);
            Blocks.set(s.at(0, h, 0).getBlock(), Material.LIGHTNING_ROD);
        }
        Location tip = s.at(0, h + 0.5, 0);
        s.every(20, i("seconds"), t -> {
            Fx.p(s.world, Particle.ELECTRIC_SPARK, tip, 10, 0.4, 0.1);
            LivingEntity target = s.nearest(tip, d("range"));
            if (target == null) return;
            Fx.line(s.world, Particle.ELECTRIC_SPARK, tip, target.getLocation().add(0, 1, 0), 0.25);
            Fx.dustLine(s.world, tip, target.getLocation().add(0, 1, 0), Color.fromRGB(180, 220, 255), 1f, 0.5);
            s.damage(target, d("damage"));
            s.sound(tip, "entity.lightning_bolt.impact", 3f, 1.5f);
        });
    }
}
