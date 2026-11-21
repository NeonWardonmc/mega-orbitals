package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.*;

/** Marks everyone nearby: for a while they take extra damage from everything. */
public final class MarkedForDeath extends Cannon {
    private final Map<UUID, Long> marked = new HashMap<>();

    public MarkedForDeath() {
        super("&4&lMarked for Death", Material.SKELETON_SKULL, "Marks everyone nearby. For a minute they", "take double damage from everything.");
        warning(10, "#C00000");
        noBlockDamage();
        option("radius", 25, "Range.");
        option("seconds", 60, "How long the mark lasts.");
        option("damage-multiplier", 2.0, "How much extra damage marked targets take.");
    }

    @Override
    public void strike(Strike s) {
        long until = System.currentTimeMillis() + i("seconds") * 1000L;
        for (LivingEntity le : s.victims(s.at(), d("radius"))) {
            marked.put(le.getUniqueId(), until);
            le.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, i("seconds") * 20, 0));
            Fx.dust(s.world, le.getLocation().add(0, 2.5, 0), Color.RED, 2f, 10, 0.2);
            if (le instanceof Player p) Fx.title(p, "&4&l☠ MARKED ☠", "&7You take x" + d("damage-multiplier") + " damage.", 40);
        }
        s.sound(s.at(), "entity.wither.ambient", 6f, 1.5f);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent e) {
        Long until = marked.get(e.getEntity().getUniqueId());
        if (until == null) return;
        if (until < System.currentTimeMillis()) { marked.remove(e.getEntity().getUniqueId()); return; }
        e.setDamage(e.getDamage() * d("damage-multiplier"));
    }
}
