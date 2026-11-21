package dev.megaorbitals;

import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/** /orbital menu - a paged chest GUI listing every cannon. Click one to take it (needs megaorbitals.give). */
public final class OrbitalMenu implements Listener {

    private static final int PER_PAGE = 45;
    private final MegaOrbitals plugin;

    public OrbitalMenu(MegaOrbitals plugin) {
        this.plugin = plugin;
    }

    private static final class Holder implements InventoryHolder {
        final int page;
        final List<Cannon> shown;
        Inventory inv;
        Holder(int page, List<Cannon> shown) { this.page = page; this.shown = shown; }
        @Override public Inventory getInventory() { return inv; }
    }

    public void open(Player p, int page) {
        List<Cannon> all = new ArrayList<>();
        for (Cannon c : plugin.cannons().values()) if (!plugin.settings().disabled(c)) all.add(c);
        int pages = Math.max(1, (all.size() + PER_PAGE - 1) / PER_PAGE);
        page = Math.max(0, Math.min(pages - 1, page));
        List<Cannon> shown = all.subList(page * PER_PAGE, Math.min(all.size(), (page + 1) * PER_PAGE));
        Holder h = new Holder(page, new ArrayList<>(shown));
        Inventory inv = plugin.getServer().createInventory(h, 54,
                MegaOrbitals.color("&4&lOrbital Cannons &8(" + (page + 1) + "/" + pages + ")"));
        h.inv = inv;
        for (int k = 0; k < shown.size(); k++) inv.setItem(k, plugin.createItem(shown.get(k), 1));
        if (page > 0) inv.setItem(45, button(Material.ARROW, "&e« Previous page"));
        inv.setItem(49, button(Material.BOOK, "&f" + all.size() + " cannons &8| &7click one to take it"));
        if (page < pages - 1) inv.setItem(53, button(Material.ARROW, "&eNext page »"));
        p.openInventory(inv);
    }

    private static ItemStack button(Material m, String name) {
        ItemStack it = new ItemStack(m);
        ItemMeta meta = it.getItemMeta();
        meta.displayName(MegaOrbitals.color(name).decoration(TextDecoration.ITALIC, false));
        it.setItemMeta(meta);
        return it;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getInventory().getHolder() instanceof Holder h)) return;
        e.setCancelled(true);
        if (!(e.getWhoClicked() instanceof Player p) || e.getClickedInventory() != e.getInventory()) return;
        int slot = e.getRawSlot();
        if (slot == 45 && h.page > 0) { open(p, h.page - 1); return; }
        if (slot == 53) { open(p, h.page + 1); return; }
        if (slot < 0 || slot >= h.shown.size()) return;
        if (!p.hasPermission("megaorbitals.give")) {
            p.sendActionBar(MegaOrbitals.color(plugin.settings().msg("no-permission")));
            return;
        }
        int amount = e.isShiftClick() ? 16 : 1;
        p.getInventory().addItem(plugin.createItem(h.shown.get(slot), amount)).values()
                .forEach(left -> p.getWorld().dropItem(p.getLocation(), left));
        p.playSound(p.getLocation(), "entity.item.pickup", 0.6f, 1.2f);
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        if (e.getInventory().getHolder() instanceof Holder) e.setCancelled(true);
    }
}
