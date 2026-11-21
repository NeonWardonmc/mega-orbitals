package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import java.util.*;

/** Self-buff: anyone who hurts you takes even more damage back. */
public final class ThornsAura extends Cannon {
    private final Map<UUID, Long> active = new HashMap<>();

    public ThornsAura() {
        super("&2&lThorns Aura", Material.CACTUS, "Self-buff: anyone who hurts you takes", "even more damage back.");
        warning(5, "#40A040");
        noBlockDamage();
        option("seconds", 60, "How long the aura lasts.");
        option("reflect", 1.5, "Damage reflected (1.5 = 150%).");
    }

    @Override
    public void strike(Strike s) {
        if (s.shooter == null) return;
        active.put(s.shooter.getUniqueId(), System.currentTimeMillis() + i("seconds") * 1000L);
        Fx.title(s.shooter, "&2&lTHORNS", "&7Attackers get hurt for " + i("seconds") + "s", 30);
        s.every(10, i("seconds") * 2, t -> {
            if (s.shooter.isOnline()) Fx.dust(s.shooter.getWorld(), s.shooter.getLocation().add(0, 1, 0), Color.GREEN, 1f, 4, 0.5);
        });
    }

    @EventHandler(ignoreCancelled = true)
    public void onHurt(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player p)) return;
        Long u = active.get(p.getUniqueId());
        if (u == null) return;
        if (u < System.currentTimeMillis()) { active.remove(p.getUniqueId()); return; }
        Entity src = e.getDamager() instanceof Projectile pr && pr.getShooter() instanceof Entity sh ? sh : e.getDamager();
        if (src instanceof LivingEntity attacker && !attacker.equals(p)) {
            double dmg = e.getFinalDamage() * d("reflect");
            plugin.getServer().getScheduler().runTask(plugin, () -> attacker.damage(dmg));
            p.getWorld().playSound(p.getLocation(), "enchant.thorns.hit", 1f, 1f);
        }
    }
}
