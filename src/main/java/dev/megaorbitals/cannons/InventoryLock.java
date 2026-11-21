package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import java.util.*;

/** Jams everyone's inventory: no opening it, no dropping, no swapping, no changing hotbar slot. */
public final class InventoryLock extends Cannon {
    private final Map<UUID, Long> locked = new HashMap<>();

    public InventoryLock() {
        super("&7&lInventory Jammer", Material.IRON_DOOR, "Jams everyone's inventory: no opening it,", "no dropping, no swapping, stuck on one slot.");
        warning(10, "#808080");
        noBlockDamage();
        option("radius", 20, "Range.");
        option("seconds", 30, "How long the jam lasts.");
    }

    @Override
    public void strike(Strike s) {
        long until = System.currentTimeMillis() + i("seconds") * 1000L;
        for (Player p : s.players(s.at(), d("radius"))) {
            locked.put(p.getUniqueId(), until);
            p.closeInventory();
            Fx.title(p, "&7&lJAMMED", "&8Your inventory is locked for " + i("seconds") + "s", 30);
        }
        s.sound(s.at(), "block.iron_door.close", 8f, 0.5f);
    }

    private boolean isLocked(Player p) {
        Long u = locked.get(p.getUniqueId());
        if (u == null) return false;
        if (u < System.currentTimeMillis()) { locked.remove(p.getUniqueId()); return false; }
        return true;
    }

    @EventHandler(ignoreCancelled = true)
    public void onOpen(InventoryOpenEvent e) { if (e.getPlayer() instanceof Player p && isLocked(p)) e.setCancelled(true); }

    @EventHandler(ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent e) { if (isLocked(e.getPlayer())) e.setCancelled(true); }

    @EventHandler(ignoreCancelled = true)
    public void onSwap(PlayerSwapHandItemsEvent e) { if (isLocked(e.getPlayer())) e.setCancelled(true); }

    @EventHandler(ignoreCancelled = true)
    public void onHeld(PlayerItemHeldEvent e) { if (isLocked(e.getPlayer())) e.setCancelled(true); }
}
