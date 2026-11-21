package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;

/** A UFO hovers over the target and beams everyone underneath up into the sky... then lets go. */
public final class Abduction extends Cannon {
    public Abduction() {
        super("&a&lAlien Abduction", Material.LIGHT_GRAY_CONCRETE, "A UFO hovers over the target and beams", "everyone underneath up... then lets go.");
        warning(20, "#60FF60");
        noBlockDamage();
        option("beam-radius", 6.0, "Width of the tractor beam.");
        option("height", 35, "UFO height.");
        option("seconds", 8, "How long the beam lifts.");
    }

    @Override
    public void strike(Strike s) {
        int h = i("height");
        double r = d("beam-radius");
        Location ufoLoc = s.at(0, h, 0);
        BlockDisplay disc = Fx.display(ufoLoc, Material.LIGHT_GRAY_CONCRETE, 9f, 1f, 9f);
        BlockDisplay dome = Fx.display(ufoLoc.clone().add(0, 1, 0), Material.LIME_STAINED_GLASS, 4f, 2f, 4f);
        Color green = Color.fromRGB(120, 255, 120);
        int ticks = i("seconds") * 20;
        s.every(1, ticks, t -> {
            if (t % 3 == 0) for (double y = 0; y < h; y += 2) Fx.dustRing(s.world, s.at(0, y, 0), r * (0.4 + 0.6 * y / h), green, 1f);
            for (Entity e : s.world.getNearbyEntities(s.at(0, h / 2.0, 0), r, h / 2.0 + 2, r)) {
                if (!(e instanceof LivingEntity le) || s.spared(e)) continue;
                Vector in = s.at().toVector().subtract(le.getLocation().toVector()).setY(0).multiply(0.05);
                double y = le.getLocation().getY() - s.at().getY();
                le.setVelocity(in.setY(y < h - 4 ? 0.35 : 0.0));
                le.setFallDistance(0);
            }
            if (t % 20 == 0) s.sound(ufoLoc, "block.beacon.ambient", 8f, 2f);
        });
        s.later(ticks, () -> {
            disc.remove();
            dome.remove();
            s.sound(ufoLoc, "entity.shulker.teleport", 10f, 0.5f);
        });
    }
}
