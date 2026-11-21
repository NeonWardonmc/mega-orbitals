package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import java.util.*;

/** A protective dome: every player inside is completely immune to damage for a while. */
public final class Aegis extends Cannon {
    private record Zone(Location centre, double radius, long until) {}
    private final List<Zone> zones = new ArrayList<>();

    public Aegis() {
        super("&e&lAegis Dome", Material.SHIELD, "A golden dome: every player inside", "is immune to all damage for 30 seconds.");
        warning(10, "#FFD040");
        noBlockDamage();
        option("radius", 15, "Dome radius.");
        option("seconds", 30, "How long the dome lasts.");
    }

    @Override
    public void strike(Strike s) {
        Zone z = new Zone(s.at(), d("radius"), System.currentTimeMillis() + i("seconds") * 1000L);
        zones.add(z);
        s.every(5, i("seconds") * 4, t -> {
            Fx.dustSphere(s.world, s.at(), d("radius"), Color.fromRGB(255, 210, 60), 1.2f, 250);
            if (t % 8 == 0) s.sound(s.at(), "block.beacon.ambient", 4f, 1.5f);
        });
        s.later(i("seconds") * 20L, () -> {
            zones.remove(z);
            s.sound(s.at(), "block.beacon.deactivate", 6f, 1f);
        });
        s.sound(s.at(), "item.shield.block", 10f, 0.6f);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player p) || zones.isEmpty()) return;
        long now = System.currentTimeMillis();
        zones.removeIf(z -> z.until < now);
        for (Zone z : zones) {
            if (z.centre.getWorld() == p.getWorld() && z.centre.distance(p.getLocation()) <= z.radius) {
                e.setCancelled(true);
                return;
            }
        }
    }
}
