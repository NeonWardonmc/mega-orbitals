package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.entity.minecart.ExplosiveMinecart;
import org.bukkit.util.Vector;

/** A runaway train of TNT minecarts barrels through the target and blows up carriage by carriage. */
public final class TntTrain extends Cannon {
    public TntTrain() {
        super("&c&lRunaway TNT Train", Material.TNT_MINECART, "A runaway train of TNT minecarts barrels", "through the target, carriage by carriage.");
        warning(25, "#FF3030");
        option("carriages", 10, "Number of TNT carts.");
        option("speed", 1.2, "Train speed.");
        option("start-distance", 30, "How far back the train starts.");
    }

    @Override
    public void strike(Strike s) {
        Vector dir = s.shooter != null ? Fx.dir(s.shooter.getLocation(), s.at()).setY(0) : new Vector(1, 0, 0);
        if (dir.lengthSquared() < 0.01) dir = new Vector(1, 0, 0);
        Vector d = dir.normalize();
        Location start = s.at().subtract(d.clone().multiply(i("start-distance")));
        for (int k = 0; k < i("carriages"); k++) {
            Location l = s.surface(start.clone().subtract(d.clone().multiply(k * 1.5))).add(0, 0.2, 0);
            ExplosiveMinecart cart = s.world.spawn(l, ExplosiveMinecart.class);
            s.tag(cart);
            s.every(1, 80, t -> { if (cart.isValid()) cart.setVelocity(d.clone().multiply(d("speed")).setY(cart.getVelocity().getY())); });
            int kk = k;
            s.later(i("start-distance") * 20L / Math.max(1, (int) (d("speed") * 20)) + 15 + kk * 3L, () -> { if (cart.isValid()) cart.explode(); });
        }
        s.sound(start, "entity.minecart.riding", 10f, 0.5f);
    }
}
