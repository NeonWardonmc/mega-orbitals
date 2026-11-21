package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

/** A herd of zoglins thunders straight through the target from the far side. */
public final class ZoglinStampede extends Cannon {
    public ZoglinStampede() {
        super("&c&lZoglin Stampede", Material.PORKCHOP, "A herd of zoglins thunders straight", "through the target from the far side.");
        warning(25, "#C06060");
        option("zoglins", 12, "Number of zoglins.");
        option("start-distance", 25, "How far away they start their charge.");
    }

    @Override
    public void strike(Strike s) {
        Vector dir = s.shooter != null ? Fx.dir(s.shooter.getLocation(), s.at()).setY(0) : new Vector(1, 0, 0);
        if (dir.lengthSquared() < 0.01) dir = new Vector(1, 0, 0);
        dir.normalize();
        Vector side = new Vector(-dir.getZ(), 0, dir.getX());
        Location start = s.at().add(dir.clone().multiply(i("start-distance")));
        Vector charge = dir.clone().multiply(-1);
        int n = i("zoglins");
        for (int k = 0; k < n; k++) {
            Location l = s.surface(start.clone().add(side.clone().multiply((k - n / 2.0) * 2)));
            Zoglin z = s.mob(l, Zoglin.class, zz -> zz.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 2)));
            s.every(2, 20, t -> { if (z.isValid()) z.setVelocity(charge.clone().multiply(0.8).setY(z.getVelocity().getY())); });
            s.later(40, () -> s.hunt(z, 40));
        }
        s.sound(start, "entity.hoglin.angry", 10f, 0.6f);
    }
}
