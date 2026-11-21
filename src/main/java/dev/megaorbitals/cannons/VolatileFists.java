package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import java.util.*;

/** Self-buff: for a while every melee hit you land explodes on your target. */
public final class VolatileFists extends Cannon {
    private final Map<UUID, Long> active = new HashMap<>();

    public VolatileFists() {
        super("&c&lVolatile Fists", Material.TNT, "Self-buff: for a while every melee hit", "you land explodes on your target.");
        warning(5, "#FF4040");
        noBlockDamage();
        option("seconds", 30, "How long the buff lasts.");
        option("power", 1.8, "Explosion power per hit.");
    }

    @Override
    public void strike(Strike s) {
        if (s.shooter == null) return;
        active.put(s.shooter.getUniqueId(), System.currentTimeMillis() + i("seconds") * 1000L);
        Fx.title(s.shooter, "&c&lVOLATILE", "&7Your punches explode for " + i("seconds") + "s", 30);
        s.shooter.playSound(s.shooter.getLocation(), "entity.creeper.primed", 1f, 1.5f);
    }

    @EventHandler(ignoreCancelled = true)
    public void onHit(EntityDamageByEntityEvent e) {
        if (e.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK || !(e.getDamager() instanceof Player p)) return;
        Long u = active.get(p.getUniqueId());
        if (u == null) return;
        if (u < System.currentTimeMillis()) { active.remove(p.getUniqueId()); return; }
        Location l = e.getEntity().getLocation();
        plugin.getServer().getScheduler().runTask(plugin, () -> l.getWorld().createExplosion(l, (float) d("power"), false, false, p));
    }
}
