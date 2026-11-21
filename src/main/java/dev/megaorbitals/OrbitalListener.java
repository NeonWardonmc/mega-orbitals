package dev.megaorbitals;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public final class OrbitalListener implements Listener {

    private final MegaOrbitals plugin;

    public OrbitalListener(MegaOrbitals plugin) {
        this.plugin = plugin;
    }

    // Right-clicking a cannon item fires it (anyone with megaorbitals.use, default everyone).
    @EventHandler(priority = EventPriority.HIGH)
    public void onUse(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        ItemStack item = e.getItem();
        if (item == null || !item.hasItemMeta()) return;
        String id = item.getItemMeta().getPersistentDataContainer().get(plugin.cannonKey, PersistentDataType.STRING);
        if (id == null) return;

        e.setUseItemInHand(Event.Result.DENY);
        e.setUseInteractedBlock(Event.Result.DENY);
        e.setCancelled(true);

        Player p = e.getPlayer();
        Settings st = plugin.settings();
        EquipmentSlot hand = e.getHand() == null ? EquipmentSlot.HAND : e.getHand();
        if (hand == EquipmentSlot.OFF_HAND && !st.allowOffHand()) return;
        if (!p.hasPermission("megaorbitals.use")) {
            p.sendActionBar(MegaOrbitals.color(st.msg("no-permission")));
            return;
        }
        if (st.requireSneak() && !p.isSneaking()) {
            p.sendActionBar(MegaOrbitals.color(st.msg("sneak-required")));
            return;
        }
        Cannon c = plugin.cannon(id);
        if (c == null) {
            p.sendActionBar(MegaOrbitals.color("&cThis cannon no longer exists."));
            return;
        }
        if (!plugin.fire(c, p, true)) return;
        if (st.consumeOnUse()) {
            item.setAmount(item.getAmount() - 1);
            p.getInventory().setItem(hand, item.getAmount() <= 0 ? null : item);
        }
    }

    private Cannon cannonOf(Entity e) {
        return plugin.cannon(e.getPersistentDataContainer().get(plugin.cannonKey, PersistentDataType.STRING));
    }

    // Per-cannon "break-blocks" setting for TNT, fireballs, creepers, withers...
    @EventHandler(ignoreCancelled = true)
    public void onExplode(EntityExplodeEvent e) {
        Cannon c = cannonOf(e.getEntity());
        if (c != null && !c.breaksBlocks()) e.blockList().clear();
    }

    // Falling blocks: obey break-blocks, and turn into "land-as" block when they land.
    @EventHandler(ignoreCancelled = true)
    public void onLand(EntityChangeBlockEvent e) {
        if (!(e.getEntity() instanceof FallingBlock fb)) return;
        Cannon c = cannonOf(fb);
        if (c == null) return;
        if (!c.breaksBlocks() || fb.getPersistentDataContainer().has(plugin.vanishKey, PersistentDataType.BYTE)) {
            e.setCancelled(true);
            fb.remove();
            return;
        }
        String land = fb.getPersistentDataContainer().get(plugin.landKey, PersistentDataType.STRING);
        if (land != null) {
            Material m = Material.matchMaterial(land);
            if (m != null) {
                e.setCancelled(true);
                fb.remove();
                Block b = e.getBlock();
                b.setType(m, true);
            }
        }
    }

    // Mobs dropped from orbit survive their first landing.
    @EventHandler(ignoreCancelled = true)
    public void onFall(EntityDamageEvent e) {
        if (e.getCause() != EntityDamageEvent.DamageCause.FALL) return;
        var pdc = e.getEntity().getPersistentDataContainer();
        if (pdc.has(plugin.noFallKey, PersistentDataType.BYTE)) {
            pdc.remove(plugin.noFallKey);
            e.setCancelled(true);
        }
    }

    // Clean up orbital arrows / tridents after they hit.
    @EventHandler
    public void onHit(ProjectileHitEvent e) {
        Entity proj = e.getEntity();
        if (cannonOf(proj) == null) return;
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> { if (proj.isValid()) proj.remove(); }, 40L);
    }
}
