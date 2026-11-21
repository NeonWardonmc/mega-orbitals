package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;

/** Channelling tridents are hurled at each target; every hit calls down lightning. */
public final class TridentBarrage extends Cannon {
    public TridentBarrage() {
        super("&3&lTrident Barrage", Material.TRIDENT, "Tridents are hurled at every target", "and each hit calls down lightning.");
        warning(20, "#20A0C0");
        option("radius", 30, "Range.");
        option("volleys", 5, "Number of volleys.");
        option("damage", 10.0, "Trident damage.");
    }

    @Override
    public void strike(Strike s) {
        s.every(12, i("volleys"), t -> {
            for (LivingEntity le : s.victims(s.at(), d("radius"))) {
                Location from = le.getLocation().add(Math.random() * 10 - 5, 25, Math.random() * 10 - 5);
                Vector v = Fx.dir(from, le.getLocation().add(0, 1, 0)).multiply(3);
                Trident tr = s.world.spawn(from, Trident.class, x -> {
                    x.setVelocity(v);
                    x.setDamage(d("damage"));
                    x.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
                    if (s.shooter != null) x.setShooter(s.shooter);
                });
                s.tag(tr);
                s.loop(1, k -> {
                    if (!tr.isValid() || k > 60) return false;
                    if (!tr.isInBlock() && tr.getLocation().distance(le.getLocation()) > 2) return true;
                    s.world.strikeLightningEffect(tr.getLocation());
                    if (tr.getLocation().distance(le.getLocation()) < 3) s.damage(le, 4);
                    tr.remove();
                    return false;
                });
            }
            s.sound(s.at(), "item.trident.throw", 6f, 0.6f);
        });
    }
}
