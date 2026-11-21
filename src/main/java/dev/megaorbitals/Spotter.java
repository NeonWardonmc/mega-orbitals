package dev.megaorbitals;

import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Color;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.RayTraceResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Spotter Binoculars: left-click to mark a target, then any cannon you fire lands on the mark instead of your
 * crosshair, so you can fire from cover. Sneak + left-click clears the mark. Right-click still zooms.
 */
public final class Spotter implements Listener {

    private record Mark(Location block, long until) {}

    private final MegaOrbitals plugin;
    private final Map<UUID, Mark> marks = new HashMap<>();

    public Spotter(MegaOrbitals plugin) {
        this.plugin = plugin;
        plugin.getServer().getScheduler().runTaskTimer(plugin, this::showMarks, 10L, 10L);
    }

    public ItemStack binoculars() {
        ItemStack it = new ItemStack(Material.SPYGLASS);
        ItemMeta m = it.getItemMeta();
        m.displayName(MegaOrbitals.color("&e&lSpotter Binoculars").decoration(TextDecoration.ITALIC, false));
        m.lore(List.of(
                MegaOrbitals.color("&eLeft-click &7to mark a target.").decoration(TextDecoration.ITALIC, false),
                MegaOrbitals.color("&7Your cannons then hit the mark,").decoration(TextDecoration.ITALIC, false),
                MegaOrbitals.color("&7wherever you fire them from.").decoration(TextDecoration.ITALIC, false),
                MegaOrbitals.color("&eSneak + left-click &7to clear it.").decoration(TextDecoration.ITALIC, false)));
        m.setEnchantmentGlintOverride(true);
        m.getPersistentDataContainer().set(plugin.itemKey, PersistentDataType.STRING, "binoculars");
        it.setItemMeta(m);
        return it;
    }

    private boolean isBinoculars(ItemStack it) {
        return it != null && it.hasItemMeta()
                && "binoculars".equals(it.getItemMeta().getPersistentDataContainer().get(plugin.itemKey, PersistentDataType.STRING));
    }

    /** The player's active mark (block location), or null. */
    public Location mark(Player p) {
        Mark m = marks.get(p.getUniqueId());
        if (m == null) return null;
        if (m.until < System.currentTimeMillis()) {
            marks.remove(p.getUniqueId());
            p.sendActionBar(MegaOrbitals.color(plugin.settings().msg("mark-expired")));
            return null;
        }
        return m.block.clone();
    }

    /** Called after the player fires; clears the mark if configured to. */
    public void fired(Player p) {
        if (plugin.getConfig().getBoolean("spotter.clear-after-fire", false)) marks.remove(p.getUniqueId());
    }

    @EventHandler
    public void onClick(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;
        if (e.getAction() != Action.LEFT_CLICK_AIR && e.getAction() != Action.LEFT_CLICK_BLOCK) return;
        if (!isBinoculars(e.getItem())) return;
        e.setCancelled(true);
        Player p = e.getPlayer();
        if (p.isSneaking()) {
            marks.remove(p.getUniqueId());
            p.sendActionBar(MegaOrbitals.color(plugin.settings().msg("mark-cleared")));
            return;
        }
        double range = plugin.getConfig().getDouble("spotter.range", 512);
        RayTraceResult r = p.getWorld().rayTraceBlocks(p.getEyeLocation(), p.getEyeLocation().getDirection(), range, FluidCollisionMode.ALWAYS, true);
        if (r == null || r.getHitBlock() == null) {
            p.sendActionBar(MegaOrbitals.color(plugin.settings().msg("no-target")));
            return;
        }
        Location b = r.getHitBlock().getLocation();
        long until = System.currentTimeMillis() + plugin.getConfig().getLong("spotter.mark-seconds", 180) * 1000L;
        marks.put(p.getUniqueId(), new Mark(b, until));
        p.sendActionBar(MegaOrbitals.color(plugin.settings().msg("mark-set")
                .replace("{x}", String.valueOf(b.getBlockX())).replace("{y}", String.valueOf(b.getBlockY()))
                .replace("{z}", String.valueOf(b.getBlockZ())).replace("{distance}", String.valueOf((int) b.distance(p.getLocation())))));
        p.playSound(p.getLocation(), "block.note_block.bit", 1f, 2f);
    }

    /** Each player sees their own mark as a red beam and ring (only they can see it). */
    private void showMarks() {
        long now = System.currentTimeMillis();
        Particle.DustOptions red = new Particle.DustOptions(Color.fromRGB(255, 40, 40), 1.5f);
        for (Map.Entry<UUID, Mark> en : marks.entrySet()) {
            Player p = plugin.getServer().getPlayer(en.getKey());
            Mark m = en.getValue();
            if (p == null || m.until < now || p.getWorld() != m.block.getWorld()) continue;
            Location top = m.block.clone().add(0.5, 1.1, 0.5);
            for (int k = 0; k < 16; k++) {
                double a = Math.PI * 2 * k / 16;
                p.spawnParticle(Particle.DUST, top.clone().add(Math.cos(a) * 1.2, 0, Math.sin(a) * 1.2), 1, 0, 0, 0, 0, red, true);
            }
            for (double y = 0; y < 25; y += 1.5) p.spawnParticle(Particle.DUST, top.clone().add(0, y, 0), 1, 0, 0, 0, 0, red, true);
        }
        marks.entrySet().removeIf(en -> en.getValue().until < now);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        marks.remove(e.getPlayer().getUniqueId());
    }
}
