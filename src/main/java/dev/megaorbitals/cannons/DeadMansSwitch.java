package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import java.util.*;

/** Rigs everyone nearby with a dead man's switch: if they die, they explode. Kill them carefully. */
public final class DeadMansSwitch extends Cannon {
    private final Map<UUID, Long> rigged = new HashMap<>();

    public DeadMansSwitch() {
        super("&c&lDead Man's Switch", Material.TNT, "Rigs everyone nearby: if they die,", "they explode. Kill them carefully.");
        warning(10, "#FF3030");
        option("radius", 25, "Range.");
        option("minutes", 3, "How long the rig lasts.");
        option("power", 5.0, "Explosion power on death.");
    }

    @Override
    public void strike(Strike s) {
        long until = System.currentTimeMillis() + i("minutes") * 60_000L;
        for (LivingEntity le : s.victims(s.at(), d("radius"))) {
            rigged.put(le.getUniqueId(), until);
            if (le instanceof Player p) Fx.title(p, "&c&l💣 RIGGED", "&7You explode when you die.", 30);
        }
        s.sound(s.at(), "entity.tnt.primed", 6f, 1.5f);
    }

    @EventHandler
    public void onDeath(EntityDeathEvent e) {
        Long u = rigged.remove(e.getEntity().getUniqueId());
        if (u == null || u < System.currentTimeMillis()) return;
        Location l = e.getEntity().getLocation();
        float power = (float) d("power");
        boolean breaks = breaksBlocks();
        plugin.getServer().getScheduler().runTask(plugin, () -> l.getWorld().createExplosion(l, power, false, breaks));
    }
}
