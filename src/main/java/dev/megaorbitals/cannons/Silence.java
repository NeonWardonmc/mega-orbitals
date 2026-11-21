package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/** A silence field: everyone caught in it can't type in chat for a while. */
public final class Silence extends Cannon {
    private final Map<UUID, Long> muted = new ConcurrentHashMap<>();

    public Silence() {
        super("&7&lSilence", Material.WHITE_WOOL, "A silence field: everyone caught in it", "can't type in chat for a while.");
        warning(10, "#C0C0C0");
        noBlockDamage();
        option("radius", 25, "Range.");
        option("seconds", 60, "How long they're muted.");
    }

    @Override
    public void strike(Strike s) {
        long until = System.currentTimeMillis() + i("seconds") * 1000L;
        for (Player p : s.players(s.at(), d("radius"))) {
            muted.put(p.getUniqueId(), until);
            Fx.title(p, "&7&l🔇", "&8You've been silenced for " + i("seconds") + "s", 30);
        }
        Fx.ring(s.world, Particle.WHITE_ASH, s.at(0, 1, 0), d("radius"), 100);
        s.sound(s.at(), "block.wool.place", 8f, 0.5f);
    }

    @EventHandler(ignoreCancelled = true)
    public void onChat(AsyncChatEvent e) {
        Long u = muted.get(e.getPlayer().getUniqueId());
        if (u == null) return;
        if (u < System.currentTimeMillis()) { muted.remove(e.getPlayer().getUniqueId()); return; }
        e.setCancelled(true);
        e.getPlayer().sendActionBar(MegaOrbitals.color("&7You are silenced."));
    }
}
