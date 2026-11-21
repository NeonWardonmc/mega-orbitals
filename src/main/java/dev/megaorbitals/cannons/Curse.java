package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.*;

/** Curses everyone nearby: they can't heal at all (no regen, no food, no potions) for a minute. */
public final class Curse extends Cannon {
    private final Map<UUID, Long> cursed = new HashMap<>();

    public Curse() {
        super("&4&lCurse of Decay", Material.WITHER_ROSE, "Everyone nearby can't heal at all.", "No regen, no food, no potions, no apples.");
        warning(20, "#600000");
        noBlockDamage();
        option("radius", 25, "Range.");
        option("seconds", 60, "How long the curse lasts.");
    }

    @Override
    public void strike(Strike s) {
        long until = System.currentTimeMillis() + i("seconds") * 1000L;
        for (LivingEntity le : s.victims(s.at(), d("radius"))) {
            cursed.put(le.getUniqueId(), until);
            le.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 100, 1));
            le.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, i("seconds") * 20, 0));
            Fx.p(s.world, Particle.SOUL, le.getLocation().add(0, 1, 0), 30, 0.4, 0.02);
            if (le instanceof Player p) Fx.title(p, "&4&lCURSED", "&7You cannot heal for " + i("seconds") + " seconds.", 40);
        }
        s.sound(s.at(), "entity.wither.ambient", 8f, 0.5f);
    }

    @EventHandler(ignoreCancelled = true)
    public void onHeal(EntityRegainHealthEvent e) {
        Long until = cursed.get(e.getEntity().getUniqueId());
        if (until == null) return;
        if (until < System.currentTimeMillis()) { cursed.remove(e.getEntity().getUniqueId()); return; }
        e.setCancelled(true);
    }
}
