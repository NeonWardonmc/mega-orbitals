package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import java.util.*;

/** Turns everyone into kindling: any damage at all sets them on fire. */
public final class Kindling extends Cannon {
    private final Map<UUID, Long> cursed = new HashMap<>();

    public Kindling() {
        super("&6&lKindling Curse", Material.STICK, "Turns everyone into kindling: any damage", "at all sets them on fire.");
        warning(10, "#FF9030");
        noBlockDamage();
        option("radius", 25, "Range.");
        option("seconds", 90, "How long the curse lasts.");
        option("burn-seconds", 5, "How long each hit makes them burn.");
    }

    @Override
    public void strike(Strike s) {
        long until = System.currentTimeMillis() + i("seconds") * 1000L;
        for (LivingEntity le : s.victims(s.at(), d("radius"))) {
            cursed.put(le.getUniqueId(), until);
            Fx.p(s.world, Particle.SMOKE, le.getLocation().add(0, 1, 0), 20, 0.4, 0.02);
            if (le instanceof Player p) Fx.title(p, "&6&lKINDLING", "&7Every hit sets you alight.", 30);
        }
        s.sound(s.at(), "item.flintandsteel.use", 8f, 0.5f);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent e) {
        if (e.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK || e.getCause() == EntityDamageEvent.DamageCause.FIRE) return;
        Long u = cursed.get(e.getEntity().getUniqueId());
        if (u == null) return;
        if (u < System.currentTimeMillis()) { cursed.remove(e.getEntity().getUniqueId()); return; }
        e.getEntity().setFireTicks(Math.max(e.getEntity().getFireTicks(), i("burn-seconds") * 20));
    }
}
