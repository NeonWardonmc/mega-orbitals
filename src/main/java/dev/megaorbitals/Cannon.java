package dev.megaorbitals;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Base class for every orbital cannon. Each cannon is its own class in
 * dev.megaorbitals.cannons and is picked up automatically.
 *
 * Its config lives in configs/<id>.yml, where id = class name in lower case.
 * Settings declared with {@link #option} appear under "settings:" in that file.
 *
 * Cannons are also registered as event listeners, so a cannon can use @EventHandler methods.
 *
 * Other plugins can add their own cannons: extend this class and register an instance with
 * {@link dev.megaorbitals.api.MegaOrbitalsAPI#register}. See docs/addons.md.
 */
public abstract class Cannon implements Listener {

    private final String defName;
    private final Material defItem;
    private final List<String> defLore;
    private int defWarning = 40;
    private String defColor = "#FF2020";
    private boolean defBreak = true;
    private final Map<String, Object> options = new LinkedHashMap<>();
    private final Map<String, String> notes = new LinkedHashMap<>();

    protected MegaOrbitals plugin;
    private YamlConfiguration cfg = new YamlConfiguration();

    protected Cannon(String name, Material item, String... lore) {
        this.defName = name;
        this.defItem = item;
        this.defLore = List.of(lore);
    }

    // ------------------------------------------------------------ setup helpers (constructor)

    /** Default warning time in ticks and colour of the targeting ring. */
    protected final void warning(int ticks, String hexColor) {
        this.defWarning = ticks;
        this.defColor = hexColor;
    }

    /** This cannon does not break blocks by default. */
    protected final void noBlockDamage() {
        this.defBreak = false;
    }

    /**
     * Default tier (common, rare, epic, legendary) and menu category for this cannon. Built-in cannons are listed in
     * {@link Catalog}; addon cannons call this in their constructor. A new category becomes a new menu tab.
     * Server owners can still override both in the cannon's yml.
     */
    protected final void catalog(String tier, String category) {
        Catalog.define(id(), tier.toLowerCase(Locale.ROOT), category.toLowerCase(Locale.ROOT));
    }

    /** Declares a setting shown under settings: in the cannon's yml. */
    protected final void option(String key, Object def, String note) {
        options.put(key, def);
        if (note != null) notes.put(key, note);
    }

    // ------------------------------------------------------------ the cannon itself

    /** Called once the warning is over. Do the cannon's thing here. */
    public abstract void strike(Strike s);

    /** The warning shown before {@link #strike}. Override for a custom warning. */
    public void warn(Strike s) {
        Fx.orbitalWarning(s, warningTicks(), warningColor(), warningRadius());
    }

    /** Size of the default warning ring. */
    protected double warningRadius() {
        return options.containsKey("radius") ? Math.min(40, Math.max(4, d("radius"))) : 8;
    }

    // ------------------------------------------------------------ config access

    public String id() { return getClass().getSimpleName().toLowerCase(Locale.ROOT); }
    public String name() { return cfg.getString("name", defName); }
    public List<String> lore() { return cfg.contains("lore") ? cfg.getStringList("lore") : defLore; }
    public Material item() {
        Material m = Material.matchMaterial(cfg.getString("item", defItem.name()));
        return m == null || !m.isItem() ? defItem : m;
    }
    public int warningTicks() { return cfg.getInt("warning-ticks", defWarning); }
    /** common, rare, epic or legendary. */
    public String tier() { return cfg.getString("tier", Catalog.tier(id())).toLowerCase(Locale.ROOT); }
    /** Menu category, e.g. explosives, mobs, terrain. */
    public String category() { return cfg.getString("category", Catalog.category(id())).toLowerCase(Locale.ROOT); }
    /** Seconds before the terrain is put back automatically (-1 = use config.yml, 0 = never). */
    public int autoRestoreSeconds() { return cfg.getInt("auto-restore-seconds", -1); }
    public Color warningColor() { return Fx.hex(cfg.getString("warning-color", defColor)); }
    public boolean breaksBlocks() {
        if (plugin != null && !plugin.settings().allowBlockDamage()) return false;
        return cfg.getBoolean("break-blocks", defBreak);
    }
    public boolean hurtsShooter() {
        Boolean override = plugin == null ? null : plugin.settings().hurtShooterOverride();
        return override != null ? override : cfg.getBoolean("hurt-shooter", false);
    }

    public int i(String key) { return cfg.getInt("settings." + key, ((Number) options.getOrDefault(key, 0)).intValue()); }
    public double d(String key) { return cfg.getDouble("settings." + key, ((Number) options.getOrDefault(key, 0)).doubleValue()); }
    public boolean b(String key) { return cfg.getBoolean("settings." + key, (Boolean) options.getOrDefault(key, false)); }
    public String str(String key) { return cfg.getString("settings." + key, String.valueOf(options.getOrDefault(key, ""))); }
    public List<String> list(String key) {
        if (cfg.isList("settings." + key)) return cfg.getStringList("settings." + key);
        Object def = options.get(key);
        List<String> out = new ArrayList<>();
        if (def instanceof List<?> l) for (Object o : l) out.add(String.valueOf(o));
        return out;
    }
    public Material mat(String key) {
        Material m = Material.matchMaterial(str(key));
        return m == null ? Material.STONE : m;
    }

    /** Loads (and creates / fills in) configs/<id>.yml */
    public final void load(MegaOrbitals plugin, File dir) {
        this.plugin = plugin;
        File f = new File(dir, id() + ".yml");
        YamlConfiguration c = YamlConfiguration.loadConfiguration(f);
        boolean changed = fillDefaults(c) || !f.exists();
        if (changed) {
            try { c.save(f); } catch (Exception ex) { plugin.getLogger().warning("Could not save " + f + ": " + ex.getMessage()); }
        }
        this.cfg = c;
    }

    /** Writes this cannon's default config to dir/<id>.yml (used at build time to bundle configs). */
    public final void export(File dir) throws java.io.IOException {
        YamlConfiguration c = new YamlConfiguration();
        fillDefaults(c);
        c.save(new File(dir, id() + ".yml"));
    }

    /** Adds every missing key (with comments) to c. Returns true if anything was added. */
    private boolean fillDefaults(YamlConfiguration c) {
        boolean changed = false;
        changed |= def(c, "name", defName, "Display name of the cannon item (& colour codes).");
        changed |= def(c, "lore", new ArrayList<>(defLore), "Description lines on the item.");
        changed |= def(c, "item", defItem.name(), "Item type of the cannon item.");
        changed |= def(c, "tier", Catalog.tier(id()), "common, rare, epic or legendary (sets crafting core, drop pool and satellite charge cost).");
        changed |= def(c, "category", Catalog.category(id()), "Menu tab this cannon is listed under.");
        changed |= def(c, "auto-restore-seconds", -1, "Put the terrain back after this many seconds (-1 = use config.yml, 0 = never).");
        changed |= def(c, "warning-ticks", defWarning, "Warning time before the strike hits (20 ticks = 1 second).");
        changed |= def(c, "warning-color", defColor, "Colour of the targeting ring.");
        changed |= def(c, "break-blocks", defBreak, "Whether this cannon may break / change blocks.");
        changed |= def(c, "hurt-shooter", false, "Whether the person who fired it can be hurt by it.");
        for (Map.Entry<String, Object> e : options.entrySet()) {
            changed |= def(c, "settings." + e.getKey(), e.getValue(), notes.get(e.getKey()));
        }
        if (changed) {
            List<String> header = new ArrayList<>();
            header.add(id() + ".yml - MegaOrbitals cannon: " + defName.replaceAll("&.", ""));
            for (String l : defLore) header.add("  " + l.replaceAll("&.", ""));
            header.add("Fire with /orbital " + id() + "   Give with /orbitalgive " + id());
            header.add("Run /orbital reload after editing.");
            c.options().setHeader(header);
        }
        return changed;
    }

    private static boolean def(YamlConfiguration c, String path, Object value, String note) {
        if (c.contains(path)) return false;
        c.set(path, value);
        if (note != null) c.setComments(path, List.of(note));
        return true;
    }
}
