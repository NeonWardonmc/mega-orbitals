package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/** A screeching cloud of bats engulfs everyone, blocking their view and nipping at them. */
public final class BatSwarm extends Cannon {
    public BatSwarm() {
        super("&8&lBat Swarm", Material.BAT_SPAWN_EGG, "A screeching cloud of bats engulfs everyone,", "blocking their view and nipping at them.");
        warning(15, "#303030");
        noBlockDamage();
        option("bats", 50, "Number of bats.");
        option("radius", 20, "Range.");
        option("seconds", 30, "How long the swarm lasts.");
        option("damage", 0.5, "Nip damage every second.");
    }

    @Override
    public void strike(Strike s) {
        var bats = new java.util.ArrayList<Bat>();
        for (int k = 0; k < i("bats"); k++) bats.add(s.expire(s.mob(s.randomPoint(5).add(0, 2, 0), Bat.class, null), i("seconds") * 20L));
        s.every(10, i("seconds") * 2, t -> {
            var targets = s.victims(s.at(), d("radius"));
            targets.removeIf(e -> e instanceof Bat);
            if (targets.isEmpty()) return;
            for (int k = 0; k < bats.size(); k++) {
                Bat b = bats.get(k);
                if (!b.isValid()) continue;
                LivingEntity tgt = targets.get(k % targets.size());
                b.setVelocity(Fx.dir(b.getLocation(), tgt.getEyeLocation()).multiply(0.6));
            }
            if (t % 2 == 0) for (LivingEntity le : targets) {
                s.damage(le, d("damage"));
                le.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 25, 0, false, false));
            }
        });
        s.sound(s.at(), "entity.bat.takeoff", 10f, 0.8f);
    }
}
