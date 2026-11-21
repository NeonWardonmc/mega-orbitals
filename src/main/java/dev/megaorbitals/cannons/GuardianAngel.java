package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import java.util.*;

/** A guardian angel watches over you: the next hit that would kill you is stopped and you're healed. */
public final class GuardianAngel extends Cannon {
    private final Map<UUID, Long> guarded = new HashMap<>();

    public GuardianAngel() {
        super("&f&lGuardian Angel", Material.TOTEM_OF_UNDYING, "A guardian angel watches over you: the next hit", "that would kill you is stopped and you're healed.");
        warning(5, "#FFFFE0");
        noBlockDamage();
        option("minutes", 5, "How long the angel watches over you.");
        option("heal-fraction", 0.6, "How much health you're restored to.");
    }

    @Override
    public void strike(Strike s) {
        if (s.shooter == null) return;
        guarded.put(s.shooter.getUniqueId(), System.currentTimeMillis() + i("minutes") * 60_000L);
        Fx.title(s.shooter, "&f&l✟", "&7A guardian angel watches over you.", 30);
        s.shooter.playSound(s.shooter.getLocation(), "block.beacon.power_select", 1f, 2f);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player p)) return;
        Long u = guarded.get(p.getUniqueId());
        if (u == null) return;
        if (u < System.currentTimeMillis()) { guarded.remove(p.getUniqueId()); return; }
        if (e.getFinalDamage() < p.getHealth()) return;
        guarded.remove(p.getUniqueId());
        e.setCancelled(true);
        var max = p.getAttribute(Attribute.MAX_HEALTH);
        p.setHealth((max == null ? 20 : max.getValue()) * d("heal-fraction"));
        p.setFireTicks(0);
        p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 60, 4));
        p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 120, 0));
        p.setVelocity(new Vector(0, 1.2, 0));
        Fx.p(p.getWorld(), Particle.TOTEM_OF_UNDYING, p.getLocation().add(0, 1, 0), 80, 0.6, 0.4);
        p.getWorld().playSound(p.getLocation(), "item.totem.use", 1f, 1f);
        Fx.title(p, "&f&l✟ SAVED ✟", "&7Your guardian angel intervened.", 40);
    }
}
