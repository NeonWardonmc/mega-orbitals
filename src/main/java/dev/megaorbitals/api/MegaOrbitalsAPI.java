package dev.megaorbitals.api;

import dev.megaorbitals.Cannon;
import dev.megaorbitals.Catalog;
import dev.megaorbitals.MegaOrbitals;
import dev.megaorbitals.Strike;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.Collection;
import java.util.List;

/**
 * The entry point for addon plugins. Add {@code depend: [MegaOrbitals]} to your plugin.yml, then in onEnable:
 *
 * <pre>{@code
 * MegaOrbitalsAPI.register(this, new MyCannon(), new MyOtherCannon());
 * }</pre>
 *
 * Registered cannons behave exactly like built-in ones: they get a configs/<id>.yml, appear in /orbital,
 * the menu, /orbitalgive, core drops and Roulette, and can be undone. They are removed automatically
 * when your plugin is disabled. See docs/addons.md for the full guide.
 *
 * Every method must be called on the main server thread.
 */
public final class MegaOrbitalsAPI {

    /** Bumped when the addon API changes in a way addons might need to check. */
    public static final int API_VERSION = 1;

    private MegaOrbitalsAPI() {}

    /** The MegaOrbitals plugin. Throws if it isn't enabled (add depend: [MegaOrbitals] to your plugin.yml). */
    public static MegaOrbitals plugin() {
        MegaOrbitals p = MegaOrbitals.instance();
        if (p == null) throw new IllegalStateException("MegaOrbitals is not enabled. Add 'depend: [MegaOrbitals]' to your plugin.yml.");
        return p;
    }

    /** True once MegaOrbitals has finished enabling. */
    public static boolean isReady() { return MegaOrbitals.instance() != null; }

    /**
     * Registers cannons owned by your plugin. A cannon is skipped (with a console warning) if its id is
     * already taken; override {@link Cannon#id()} to pick a different id. Returns how many were registered.
     */
    public static int register(Plugin owner, Cannon... cannons) {
        int n = 0;
        for (Cannon c : cannons) if (plugin().registerCannon(owner, c)) n++;
        return n;
    }

    /** Removes every cannon your plugin registered. Happens automatically when your plugin is disabled. */
    public static int unregister(Plugin owner) { return plugin().unregisterCannons(owner); }

    /** A cannon by id (built-in or from any addon), or null. */
    public static Cannon cannon(String id) { return plugin().cannon(id); }

    /** Every loaded cannon. */
    public static Collection<Cannon> cannons() { return plugin().cannons().values(); }

    /** Cannons registered by the given plugin (MegaOrbitals itself for built-in ones). */
    public static List<Cannon> cannonsOf(Plugin owner) { return plugin().cannonsOf(owner); }

    /** The plugin that registered this cannon (MegaOrbitals for built-in ones), or null if it isn't loaded. */
    public static Plugin ownerOf(Cannon cannon) { return plugin().ownerOf(cannon); }

    /**
     * Fires a cannon for a player at what they are looking at (or their spotter mark), with every config.yml
     * rule applied: cooldowns, worlds, range, satellites, announcements... asItem = treat it like right-clicking
     * a cannon item (item cooldowns, satellites, per-cannon permission). Returns true if it fired.
     */
    public static boolean fire(Player shooter, Cannon cannon, boolean asItem) {
        return plugin().fire(cannon, shooter, asItem);
    }

    /**
     * Fires a cannon at a block with no checks at all (no cooldown, permission or world rules).
     * shooter may be null. The warning plays first, then the strike lands. Returns the strike.
     */
    public static Strike fireAt(Cannon cannon, Location targetBlock, Player shooter) {
        return plugin().fireAt(cannon, targetBlock, shooter);
    }

    /** A cannon item (the same one /orbitalgive hands out). */
    public static ItemStack item(Cannon cannon, int amount) { return plugin().createItem(cannon, amount); }

    /** The cannon id stored on an item, or null if it isn't a cannon item. */
    public static String cannonIdOf(ItemStack item) { return plugin().cannonIdOf(item); }

    /** Adds a menu category (tab). Registering a cannon with a new category does this for you. */
    public static void addCategory(String category) { Catalog.addCategory(category); }
}
