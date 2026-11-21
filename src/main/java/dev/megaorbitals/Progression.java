package dev.megaorbitals;

import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Survival progression: tiered Orbital Cores that are crafted (each tier from the one below) or dropped by
 * bosses, and decode into a random cannon of their tier. Also registers the binoculars and satellite recipes.
 */
public final class Progression implements Listener {

    private final MegaOrbitals plugin;
    private final List<NamespacedKey> recipes = new ArrayList<>();

    public Progression(MegaOrbitals plugin) {
        this.plugin = plugin;
    }

    // ------------------------------------------------------------ tiers

    /** Satellite charge a cannon costs to fire. */
    public int cost(Cannon c) {
        return plugin.getConfig().getInt("satellites.cost." + c.tier(), switch (c.tier()) {
            case "common" -> 10; case "epic" -> 50; case "legendary" -> 100; default -> 25;
        });
    }

    private static Material coreMaterial(String tier) {
        return switch (tier) {
            case "common" -> Material.FIREWORK_STAR;
            case "epic" -> Material.ECHO_SHARD;
            case "legendary" -> Material.NETHER_STAR;
            default -> Material.HEART_OF_THE_SEA;
        };
    }

    public ItemStack core(String tier, int amount) {
        ItemStack it = new ItemStack(coreMaterial(tier), Math.max(1, amount));
        ItemMeta m = it.getItemMeta();
        m.displayName(MegaOrbitals.color(Catalog.tierColor(tier) + Catalog.pretty(tier) + " Orbital Core").decoration(TextDecoration.ITALIC, false));
        m.lore(List.of(
                MegaOrbitals.color("&7Right-click to decode it into a").decoration(TextDecoration.ITALIC, false),
                MegaOrbitals.color("&7random " + Catalog.tierColor(tier) + tier + " &7cannon.").decoration(TextDecoration.ITALIC, false)));
        m.setEnchantmentGlintOverride(true);
        m.getPersistentDataContainer().set(plugin.itemKey, PersistentDataType.STRING, "core:" + tier);
        it.setItemMeta(m);
        return it;
    }

    /** "core:rare" -> "rare", or null if it isn't a core. */
    public static String coreTier(MegaOrbitals plugin, ItemStack it) {
        if (it == null || !it.hasItemMeta()) return null;
        String v = it.getItemMeta().getPersistentDataContainer().get(plugin.itemKey, PersistentDataType.STRING);
        return v != null && v.startsWith("core:") ? v.substring(5) : null;
    }

    // ------------------------------------------------------------ crafting

    public void registerRecipes() {
        for (NamespacedKey k : recipes) plugin.getServer().removeRecipe(k);
        recipes.clear();
        if (!plugin.getConfig().getBoolean("crafting.enabled", true)) return;
        ConfigurationSection sec = plugin.getConfig().getConfigurationSection("crafting.recipes");
        if (sec == null) return;
        for (String name : sec.getKeys(false)) {
            ItemStack result = switch (name) {
                case "binoculars" -> plugin.spotter().binoculars();
                case "satellite" -> plugin.satellites().satelliteItem();
                default -> Catalog.TIERS.contains(name) ? core(name, 1) : null;
            };
            if (result == null) { plugin.getLogger().warning("Unknown recipe '" + name + "' in config.yml"); continue; }
            try {
                NamespacedKey key = new NamespacedKey(plugin, "recipe_" + name);
                ShapedRecipe r = new ShapedRecipe(key, result);
                r.shape(sec.getStringList(name + ".shape").toArray(new String[0]));
                ConfigurationSection ing = sec.getConfigurationSection(name + ".ingredients");
                for (String ch : ing.getKeys(false)) {
                    String v = ing.getString(ch);
                    if (v.startsWith("core:")) r.setIngredient(ch.charAt(0), new RecipeChoice.ExactChoice(core(v.substring(5), 1)));
                    else r.setIngredient(ch.charAt(0), Material.valueOf(v.toUpperCase()));
                }
                plugin.getServer().addRecipe(r);
                recipes.add(key);
            } catch (Exception e) {
                plugin.getLogger().warning("Bad recipe '" + name + "' in config.yml: " + e.getMessage());
            }
        }
        for (Player p : plugin.getServer().getOnlinePlayers()) p.discoverRecipes(recipes);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        e.getPlayer().discoverRecipes(recipes);
    }

    // ------------------------------------------------------------ decoding cores

    @EventHandler(priority = EventPriority.HIGH)
    public void onUse(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        String tier = coreTier(plugin, e.getItem());
        if (tier == null) return;
        e.setUseItemInHand(Event.Result.DENY);
        e.setCancelled(true);
        Player p = e.getPlayer();
        List<Cannon> pool = new ArrayList<>();
        for (Cannon c : plugin.cannons().values()) if (c.tier().equals(tier) && !plugin.settings().disabled(c)) pool.add(c);
        if (pool.isEmpty()) { p.sendActionBar(MegaOrbitals.color(plugin.settings().msg("core-empty"))); return; }
        Cannon pick = pool.get(ThreadLocalRandom.current().nextInt(pool.size()));
        ItemStack held = e.getItem();
        held.setAmount(held.getAmount() - 1);
        p.getInventory().setItem(e.getHand() == null ? EquipmentSlot.HAND : e.getHand(), held.getAmount() <= 0 ? null : held);
        p.getInventory().addItem(plugin.createItem(pick, 1)).values().forEach(l -> p.getWorld().dropItem(p.getLocation(), l));
        p.sendMessage(MegaOrbitals.color(plugin.settings().prefixed("core-opened").replace("{cannon}", pick.name())));
        p.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, p.getLocation().add(0, 1, 0), 40, 0.4, 0.6, 0.4, 0.3);
        p.playSound(p.getLocation(), "ui.toast.challenge_complete", 0.7f, 1.4f);
    }

    // ------------------------------------------------------------ boss drops

    @EventHandler(ignoreCancelled = true)
    public void onDeath(EntityDeathEvent e) {
        LivingEntity dead = e.getEntity();
        if (dead.getKiller() == null || dead instanceof Player) return;
        if (dead.getPersistentDataContainer().has(plugin.cannonKey, PersistentDataType.STRING)) return; // no farming cannon mobs
        ConfigurationSection drops = plugin.getConfig().getConfigurationSection("drops");
        if (drops == null || !plugin.getConfig().getBoolean("drops-enabled", true)) return;
        String entry = drops.getString(dead.getType().name());
        if (entry == null && dead instanceof Monster) entry = drops.getString("ANY_MONSTER");
        if (entry == null) return;
        String[] parts = entry.split(":");
        double chance = parts.length > 1 ? Double.parseDouble(parts[1]) : 1.0;
        if (ThreadLocalRandom.current().nextDouble() < chance) {
            e.getDrops().add(core(parts[0].toLowerCase(), 1));
            dead.getKiller().playSound(dead.getLocation(), "entity.player.levelup", 1f, 1.8f);
        }
    }

    public Map<String, Integer> tierCounts() {
        Map<String, Integer> m = new java.util.LinkedHashMap<>();
        for (String t : Catalog.TIERS) m.put(t, 0);
        for (Cannon c : plugin.cannons().values()) m.merge(c.tier(), 1, Integer::sum);
        return m;
    }
}
