package com.example.orbitaladdon;

import dev.megaorbitals.api.MegaOrbitalsAPI;
import dev.megaorbitals.api.OrbitalFireEvent;
import dev.megaorbitals.api.OrbitalStrikeEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

/** The addon's main class: registers its cannons and shows how to use the MegaOrbitals events. */
public final class ExampleAddon extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        // Each cannon gets plugins/MegaOrbitals/configs/<id>.yml and shows up in /orbital, the menu and /orbitalgive.
        int added = MegaOrbitalsAPI.register(this, new ConfettiCannon(), new PumpkinPatch());
        getLogger().info("Added " + added + " cannons to MegaOrbitals.");
        getServer().getPluginManager().registerEvents(this, this);
        // Nothing to do in onDisable: MegaOrbitals removes this addon's cannons by itself.
    }

    /** Example: no orbital strikes while gliding with an elytra. Cancelling uses nothing up (no cooldown, no charge). */
    @EventHandler
    public void onFire(OrbitalFireEvent e) {
        if (e.getPlayer().isGliding()) {
            e.setCancelled(true);
            e.getPlayer().sendActionBar(net.kyori.adventure.text.Component.text("Land before calling an orbital strike!"));
        }
    }

    /** Example: log whenever one of this addon's cannons lands. */
    @EventHandler
    public void onStrike(OrbitalStrikeEvent e) {
        if (MegaOrbitalsAPI.ownerOf(e.getCannon()) != this) return;
        var l = e.getLocation();
        getLogger().info(e.getCannon().id() + " landed at " + l.getBlockX() + " " + l.getBlockY() + " " + l.getBlockZ());
    }
}
