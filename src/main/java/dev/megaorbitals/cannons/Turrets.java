package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;

/** Drops automatic arrow turrets that fire at anyone who comes near (except you). */
public final class Turrets extends Cannon {
    public Turrets() {
        super("&7&lAuto Turrets", Material.DISPENSER, "Drops automatic arrow turrets that fire", "at anyone who comes near (except you).");
        warning(15, "#909090");
        noBlockDamage();
        option("turrets", 4, "Number of turrets.");
        option("spacing", 6.0, "Distance of turrets from the target.");
        option("range", 30.0, "Turret range.");
        option("seconds", 60, "How long the turrets stay.");
        option("arrow-damage", 4.0, "Arrow damage.");
    }

    @Override
    public void strike(Strike s) {
        for (Location l : Fx.circle(s.at(), d("spacing"), i("turrets"))) {
            Location base = s.surface(l).add(0, 0.5, 0);
            BlockDisplay body = Fx.display(base, Material.DISPENSER, 1f);
            Location muzzle = base.clone().add(0, 1, 0);
            s.every(10, i("seconds") * 2, t -> {
                LivingEntity target = s.nearest(muzzle, d("range"));
                if (target != null) {
                    Vector v = Fx.dir(muzzle, target.getEyeLocation());
                    Arrow a = s.world.spawnArrow(muzzle.clone().add(v), v, 2.5f, 1f);
                    a.setDamage(d("arrow-damage"));
                    a.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
                    if (s.shooter != null) a.setShooter(s.shooter);
                    s.expire(s.tag(a), 80);
                    s.sound(muzzle, "block.dispenser.launch", 2f, 1.2f);
                }
                if (t == i("seconds") * 2 - 1) body.remove();
            });
        }
    }
}
