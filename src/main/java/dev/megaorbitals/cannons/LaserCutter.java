package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;

/** A razor-thin orbital laser slices the world in half along your line of sight. */
public final class LaserCutter extends Cannon {
    public LaserCutter() {
        super("&c&lLaser Cutter", Material.SHEARS, "A razor-thin orbital laser slices the world", "in half along your line of sight.");
        warning(20, "#FF2020");
        option("length", 80, "Length of the cut.");
        option("depth", 40, "How deep the cut goes.");
        option("height", 25, "How high above the target it cuts.");
        option("damage", 20.0, "Damage to anything caught in the beam.");
    }

    @Override
    public void strike(Strike s) {
        Vector dir = s.shooter != null ? s.shooter.getLocation().getDirection().setY(0) : new Vector(1, 0, 0);
        if (dir.lengthSquared() < 0.01) dir = new Vector(1, 0, 0);
        Vector d = dir.normalize();
        int len = i("length"), depth = i("depth"), h = i("height");
        Location start = s.at().subtract(d.clone().multiply(len / 2.0));
        Color red = Color.fromRGB(255, 30, 30);
        s.every(1, len, t -> {
            Location col = start.clone().add(d.clone().multiply(t));
            Fx.dustLine(s.world, col.clone().add(0, 80, 0), col.clone().subtract(0, depth, 0), red, 1.5f, 1.0);
            if (s.breaks()) for (int y = -depth; y <= h; y++) Blocks.set(col.clone().add(0, y, 0).getBlock(), Material.AIR);
            for (Entity e : s.world.getNearbyEntities(col, 1.2, h + depth, 1.2)) {
                if (e instanceof LivingEntity le && !s.spared(e)) s.damage(le, d("damage"));
            }
            if (t % 5 == 0) s.sound(col, "block.beacon.power_select", 3f, 2f);
        });
    }
}
