package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import java.util.*;

/** A zone where nobody but you can place or break blocks. Perfect for sieges. */
public final class NoBuildZone extends Cannon {
    private record Zone(Location c, double r, long until, UUID owner) {}
    private final List<Zone> zones = new ArrayList<>();

    public NoBuildZone() {
        super("&c&lNo-Build Zone", Material.BEDROCK, "A zone where nobody but you can place", "or break blocks. Perfect for sieges.");
        warning(10, "#C02020");
        noBlockDamage();
        option("radius", 30.0, "Size of the zone.");
        option("seconds", 120, "How long the zone lasts.");
    }

    @Override
    public void strike(Strike s) {
        Zone z = new Zone(s.at(), d("radius"), System.currentTimeMillis() + i("seconds") * 1000L, s.shooter == null ? null : s.shooter.getUniqueId());
        zones.add(z);
        s.every(40, i("seconds") / 2, t -> Fx.dustRing(s.world, s.at(0, 0.3, 0), d("radius"), Color.RED, 1.5f));
        s.later(i("seconds") * 20L, () -> zones.remove(z));
        s.sound(s.at(), "block.beacon.activate", 6f, 0.7f);
    }

    private boolean blocked(Player p, Block b) {
        long now = System.currentTimeMillis();
        zones.removeIf(z -> z.until < now);
        for (Zone z : zones) {
            if (z.c.getWorld() != b.getWorld() || p.getUniqueId().equals(z.owner)) continue;
            if (z.c.distance(b.getLocation()) <= z.r) return true;
        }
        return false;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
        if (blocked(e.getPlayer(), e.getBlock())) { e.setCancelled(true); e.getPlayer().sendActionBar(MegaOrbitals.color("&cNo building here.")); }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent e) {
        if (blocked(e.getPlayer(), e.getBlock())) { e.setCancelled(true); e.getPlayer().sendActionBar(MegaOrbitals.color("&cNo building here.")); }
    }
}
