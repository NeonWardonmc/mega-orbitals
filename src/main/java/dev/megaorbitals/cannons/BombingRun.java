package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.util.Vector;

/** A bomber flies over in the direction you are facing, dropping a line of TNT. */
public final class BombingRun extends Cannon {
    public BombingRun() {
        super("&6&lBombing Run", Material.ELYTRA, "A bomber flies overhead in the", "direction you face, carpet bombing a line.");
        warning(20, "#FF8000");
        option("length", 120, "Length of the bombing line in blocks.");
        option("spacing", 3.0, "Blocks between each bomb.");
        option("width", 2, "Bombs dropped side by side per row.");
        option("height", 35, "Bomber altitude above the target.");
        option("power", 5.0, "Explosion power of each bomb.");
    }

    @Override
    public void strike(Strike s) {
        Vector dir = s.shooter != null ? s.shooter.getLocation().getDirection().setY(0) : new Vector(1, 0, 0);
        if (dir.lengthSquared() < 0.01) dir = new Vector(1, 0, 0);
        dir.normalize();
        Vector side = new Vector(-dir.getZ(), 0, dir.getX());
        double length = d("length"), spacing = d("spacing");
        int h = i("height"), width = i("width");
        Location start = s.at(0, h, 0).subtract(dir.clone().multiply(length / 2));
        int fuse = Strike.fallTicks(h);
        float power = (float) d("power");
        int rows = (int) (length / spacing);
        Vector stepV = dir.clone().multiply(spacing);
        s.every(2, rows, t -> {
            Location row = start.clone().add(stepV.clone().multiply(t));
            for (int k = 0; k < width; k++) {
                double off = (k - (width - 1) / 2.0) * 3;
                s.tnt(row.clone().add(side.clone().multiply(off)), power, fuse);
            }
            Fx.p(s.world, Particle.CLOUD, row.clone().add(0, 2, 0), 6, 0.6, 0.02);
            if (t % 5 == 0) s.sound(row, "entity.phantom.flap", 8f, 0.4f);
        });
    }
}
