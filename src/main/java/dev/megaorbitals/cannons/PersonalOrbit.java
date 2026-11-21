package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import java.util.*;

/** Burning magma rocks orbit around YOU, scorching anyone who gets close. Follows you everywhere. */
public final class PersonalOrbit extends Cannon {
    public PersonalOrbit() {
        super("&6&lPersonal Orbit", Material.MAGMA_BLOCK, "Burning rocks orbit around YOU, scorching", "anyone who gets close. Follows you everywhere.");
        warning(5, "#FF8020");
        noBlockDamage();
        option("rocks", 5, "Number of orbiting rocks.");
        option("radius", 3.0, "Orbit size.");
        option("seconds", 45, "How long they orbit you.");
        option("damage", 5.0, "Damage per hit.");
    }

    @Override
    public void strike(Strike s) {
        Player me = s.shooter;
        if (me == null) return;
        int n = i("rocks"), ticks = i("seconds") * 20;
        List<BlockDisplay> rocks = new ArrayList<>();
        for (int k = 0; k < n; k++) rocks.add(Fx.display(me.getLocation(), Material.MAGMA_BLOCK, 0.8f));
        Map<UUID, Integer> lastHit = new HashMap<>();
        s.every(1, ticks, t -> {
            if (!me.isOnline() || me.isDead() || t == ticks - 1) { rocks.forEach(Entity::remove); return; }
            for (int k = 0; k < n; k++) {
                double a = t * 0.2 + 2 * Math.PI * k / n;
                Location p = me.getLocation().add(Math.cos(a) * d("radius"), 1 + Math.sin(t * 0.1 + k) * 0.4, Math.sin(a) * d("radius"));
                rocks.get(k).teleport(p);
                Fx.p(me.getWorld(), Particle.FLAME, p, 1, 0.1, 0.01);
                for (Entity e : me.getWorld().getNearbyEntities(p, 1, 1, 1)) {
                    if (!(e instanceof LivingEntity le) || s.spared(e)) continue;
                    if (t - lastHit.getOrDefault(le.getUniqueId(), -100) < 10) continue;
                    lastHit.put(le.getUniqueId(), t);
                    s.damage(le, d("damage"));
                    le.setFireTicks(80);
                    le.setVelocity(Fx.dir(me.getLocation(), le.getLocation()).multiply(1).setY(0.4));
                }
            }
        });
        me.playSound(me.getLocation(), "entity.blaze.ambient", 1f, 0.8f);
    }
}
