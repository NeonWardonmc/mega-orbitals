package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import java.util.*;

/** Curses everyone with brittle bones: fall damage is multiplied five times. Watch your step. */
public final class BrittleBones extends Cannon {
    private final Map<UUID, Long> cursed = new HashMap<>();

    public BrittleBones() {
        super("&f&lBrittle Bones", Material.BONE, "Curses everyone with brittle bones: fall", "damage is multiplied five times.");
        warning(10, "#E0E0D0");
        noBlockDamage();
        option("radius", 25, "Range.");
        option("seconds", 90, "How long the curse lasts.");
        option("multiplier", 5.0, "Fall damage multiplier.");
    }

    @Override
    public void strike(Strike s) {
        long until = System.currentTimeMillis() + i("seconds") * 1000L;
        for (LivingEntity le : s.victims(s.at(), d("radius"))) {
            cursed.put(le.getUniqueId(), until);
            Fx.p(s.world, Particle.WHITE_ASH, le.getLocation().add(0, 1, 0), 30, 0.4, 0.02);
            if (le instanceof Player p) Fx.title(p, "&f&lBRITTLE BONES", "&7Don't fall.", 30);
        }
        s.sound(s.at(), "entity.skeleton.hurt", 8f, 0.5f);
    }

    @EventHandler(ignoreCancelled = true)
    public void onFall(EntityDamageEvent e) {
        if (e.getCause() != EntityDamageEvent.DamageCause.FALL) return;
        Long u = cursed.get(e.getEntity().getUniqueId());
        if (u == null) return;
        if (u < System.currentTimeMillis()) { cursed.remove(e.getEntity().getUniqueId()); return; }
        e.setDamage(e.getDamage() * d("multiplier"));
    }
}
