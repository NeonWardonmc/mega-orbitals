package dev.megaorbitals.api;

import dev.megaorbitals.Cannon;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

/**
 * A player is about to fire a cannon (with an item or /orbital). Called after MegaOrbitals' own checks
 * (disabled cannon, world, permission, cooldown, range, spawn protection, satellite charge) pass, and before
 * the cooldown starts or any charge is spent. Cancel it to stop the shot; nothing is used up.
 *
 * Not called when a cannon is fired with no checks at all (MegaOrbitalsAPI.fireAt, Omega, Roulette).
 */
public final class OrbitalFireEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Cannon cannon;
    private final Location target;
    private final boolean fromItem;
    private boolean cancelled;

    public OrbitalFireEvent(Player shooter, Cannon cannon, Location target, boolean fromItem) {
        super(shooter);
        this.cannon = cannon;
        this.target = target;
        this.fromItem = fromItem;
    }

    /** The cannon being fired. */
    public Cannon getCannon() { return cannon; }

    /** The block the strike is aimed at (a copy). */
    public Location getTarget() { return target.clone(); }

    /** True if fired by right-clicking a cannon item, false for /orbital. */
    public boolean isFromItem() { return fromItem; }

    @Override public boolean isCancelled() { return cancelled; }
    @Override public void setCancelled(boolean cancel) { this.cancelled = cancel; }

    @Override public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
