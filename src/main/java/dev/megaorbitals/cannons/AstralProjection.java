package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import java.util.*;

/** Your spirit leaves your body: fly through walls as a ghost to scout, then snap back. */
public final class AstralProjection extends Cannon {
    private record Body(Location where, GameMode mode) {}
    private final Map<UUID, Body> bodies = new HashMap<>();

    public AstralProjection() {
        super("&b&lAstral Projection", Material.PHANTOM_MEMBRANE, "Your spirit leaves your body: fly through walls", "as a ghost to scout, then snap back.");
        warning(5, "#A0E0FF");
        noBlockDamage();
        option("seconds", 20, "How long you can scout.");
    }

    @Override
    public void strike(Strike s) {
        Player me = s.shooter;
        if (me == null || bodies.containsKey(me.getUniqueId())) return;
        bodies.put(me.getUniqueId(), new Body(me.getLocation(), me.getGameMode()));
        me.setGameMode(GameMode.SPECTATOR);
        me.teleport(s.at(0, 3, 0).setDirection(me.getLocation().getDirection()));
        int secs = i("seconds");
        s.every(20, secs, t -> { if (me.isOnline()) s.actionBar(me, "&b👁 Astral form: &f" + (secs - t) + "s"); });
        s.later(secs * 20L, () -> restore(me));
    }

    private void restore(Player p) {
        Body b = bodies.remove(p.getUniqueId());
        if (b == null) return;
        p.teleport(b.where);
        p.setGameMode(b.mode);
        p.playSound(p.getLocation(), "entity.enderman.teleport", 1f, 1.5f);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) { restore(e.getPlayer()); }
}
