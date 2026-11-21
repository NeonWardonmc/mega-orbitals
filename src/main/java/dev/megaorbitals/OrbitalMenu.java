package dev.megaorbitals;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * /orbital menu: a paged chest GUI of every cannon with category tabs, a tier filter and a search box.
 * Bottom row: [prev] [category] [tier] [search] [info] ... [next]. Click a cannon to take it (needs megaorbitals.give).
 */
public final class OrbitalMenu implements Listener {

    private static final int PER_PAGE = 45;
    private static final int PREV = 45, CATEGORY = 46, TIER = 47, SEARCH = 48, INFO = 49, NEXT = 53;

    /** What a player is currently filtering by. */
    private static final class View {
        int page;
        String category; // null = all
        String tier;     // null = all
        String search;   // null = none
    }

    private final MegaOrbitals plugin;
    private final Map<UUID, View> views = new ConcurrentHashMap<>();
    private final Set<UUID> awaitingSearch = ConcurrentHashMap.newKeySet();

    public OrbitalMenu(MegaOrbitals plugin) {
        this.plugin = plugin;
    }

    private static final class Holder implements InventoryHolder {
        final List<Cannon> shown;
        Inventory inv;
        Holder(List<Cannon> shown) { this.shown = shown; }
        @Override public Inventory getInventory() { return inv; }
    }

    public void open(Player p, int page) {
        View v = views.computeIfAbsent(p.getUniqueId(), k -> new View());
        v.page = page;
        open(p);
    }

    private void open(Player p) {
        View v = views.computeIfAbsent(p.getUniqueId(), k -> new View());
        List<Cannon> all = new ArrayList<>();
        for (Cannon c : plugin.cannons().values()) {
            if (plugin.settings().disabled(c)) continue;
            if (v.category != null && !v.category.equals(c.category())) continue;
            if (v.tier != null && !v.tier.equals(c.tier())) continue;
            if (v.search != null) {
                String hay = (c.id() + " " + c.name() + " " + String.join(" ", c.lore()) + " " + c.category()).toLowerCase().replaceAll("&.", "");
                if (!hay.contains(v.search)) continue;
            }
            all.add(c);
        }
        int pages = Math.max(1, (all.size() + PER_PAGE - 1) / PER_PAGE);
        v.page = Math.max(0, Math.min(pages - 1, v.page));
        List<Cannon> shown = new ArrayList<>(all.subList(v.page * PER_PAGE, Math.min(all.size(), (v.page + 1) * PER_PAGE)));
        Holder h = new Holder(shown);
        Inventory inv = plugin.getServer().createInventory(h, 54, MegaOrbitals.color(plugin.settings().msg("menu-title")
                .replace("{page}", String.valueOf(v.page + 1)).replace("{pages}", String.valueOf(pages))));
        h.inv = inv;
        for (int k = 0; k < shown.size(); k++) inv.setItem(k, plugin.createItem(shown.get(k), 1));
        ItemStack filler = button(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int s = 45; s < 54; s++) inv.setItem(s, filler);
        if (v.page > 0) inv.setItem(PREV, button(Material.ARROW, "&e« Previous page"));
        if (v.page < pages - 1) inv.setItem(NEXT, button(Material.ARROW, "&eNext page »"));
        inv.setItem(CATEGORY, button(Material.COMPASS, "&bCategory: &f" + (v.category == null ? "All" : Catalog.pretty(v.category)),
                "&7Left-click: next", "&7Right-click: previous", "&7Shift-click: all"));
        inv.setItem(TIER, button(Material.NETHER_STAR, "&dTier: " + (v.tier == null ? "&fAll" : Catalog.tierColor(v.tier) + Catalog.pretty(v.tier)),
                "&7Click to cycle", "&7Shift-click: all"));
        inv.setItem(SEARCH, button(Material.NAME_TAG, "&eSearch: &f" + (v.search == null ? "none" : v.search),
                "&7Left-click: type a search", "&7Right-click: clear"));
        inv.setItem(INFO, button(Material.BOOK, "&f" + all.size() + " cannons shown",
                "&7Click a cannon to take one", "&7Shift-click for 16"));
        p.openInventory(inv);
    }

    private static ItemStack button(Material m, String name, String... lore) {
        ItemStack it = new ItemStack(m);
        ItemMeta meta = it.getItemMeta();
        meta.displayName(MegaOrbitals.color(name).decoration(TextDecoration.ITALIC, false));
        List<net.kyori.adventure.text.Component> l = new ArrayList<>();
        for (String s : lore) l.add(MegaOrbitals.color(s).decoration(TextDecoration.ITALIC, false));
        meta.lore(l);
        it.setItemMeta(meta);
        return it;
    }

    private static String cycle(List<String> list, String current, int dir) {
        int i = current == null ? -1 : list.indexOf(current);
        int n = i + dir;
        if (n < -1) n = list.size() - 1;
        if (n >= list.size()) n = -1;
        return n == -1 ? null : list.get(n);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getInventory().getHolder() instanceof Holder h)) return;
        e.setCancelled(true);
        if (!(e.getWhoClicked() instanceof Player p) || e.getClickedInventory() != e.getInventory()) return;
        View v = views.computeIfAbsent(p.getUniqueId(), k -> new View());
        int slot = e.getRawSlot();
        switch (slot) {
            case PREV -> { v.page--; open(p); return; }
            case NEXT -> { v.page++; open(p); return; }
            case CATEGORY -> {
                v.category = e.isShiftClick() ? null : cycle(Catalog.CATEGORIES, v.category, e.isRightClick() ? -1 : 1);
                v.page = 0; open(p); return;
            }
            case TIER -> { v.tier = e.isShiftClick() ? null : cycle(Catalog.TIERS, v.tier, 1); v.page = 0; open(p); return; }
            case SEARCH -> {
                if (e.isRightClick()) { v.search = null; v.page = 0; open(p); return; }
                awaitingSearch.add(p.getUniqueId());
                p.closeInventory();
                p.sendMessage(MegaOrbitals.color(plugin.settings().prefixed("menu-search-prompt")));
                return;
            }
            default -> { }
        }
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

    /** Typed search text arrives here (chat is async, so the menu reopens on the main thread). */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncChatEvent e) {
        Player p = e.getPlayer();
        if (!awaitingSearch.remove(p.getUniqueId())) return;
        e.setCancelled(true);
        String text = PlainTextComponentSerializer.plainText().serialize(e.message()).trim().toLowerCase();
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            View v = views.computeIfAbsent(p.getUniqueId(), k -> new View());
            if (text.equals("cancel") || text.isEmpty()) {
                p.sendMessage(MegaOrbitals.color(plugin.settings().prefixed("menu-search-cancelled")));
            } else {
                v.search = text;
                v.page = 0;
            }
            open(p);
        });
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        if (e.getInventory().getHolder() instanceof Holder) e.setCancelled(true);
    }
}
