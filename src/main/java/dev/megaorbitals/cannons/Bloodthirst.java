package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import java.util.*;

/** Self-buff: every hit you land heals you for part of the damage dealt. */
public final class Bloodthirst extends Cannon {
    private final Map<UUID, Long> active = new HashMap<>();

    public Bloodthirst() {
        super("&4&lBloodthirst", Material.RED_DYE, "Self-buff: every hit you land heals you", "for part of the damage dealt.");
        warning(5, "#A00010");
        noBlockDamage();
        option("seconds", 60, "How long the buff lasts.");
        option("lifesteal", 0.5, "Fraction of damage dealt that heals you.");
    }

    @Override
    public void strike(Strike s) {
        if (s.shooter == null) return;
        active.put(s.shooter.getUniqueId(), System.currentTimeMillis() + i("seconds") * 1000L);
        Fx.title(s.shooter, "&4&lBLOODTHIRST", "&7Your hits heal you for " + i("seconds") + "s", 30);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHit(EntityDamageByEntityEvent e) {
        Entity src = e.getDamager() instanceof Projectile pr && pr.getShooter() instanceof Entity sh ? sh : e.getDamager();
        if (!(src instanceof Player p)) return;
        Long u = active.get(p.getUniqueId());
        if (u == null) return;
        if (u < System.currentTimeMillis()) { active.remove(p.getUniqueId()); return; }
        var max = p.getAttribute(Attribute.MAX_HEALTH);
        double cap = max == null ? 20 : max.getValue();
        p.setHealth(Math.min(cap, p.getHealth() + e.getFinalDamage() * d("lifesteal")));
        Fx.dust(p.getWorld(), p.getLocation().add(0, 1, 0), Color.RED, 1.2f, 6, 0.3);
    }
}
