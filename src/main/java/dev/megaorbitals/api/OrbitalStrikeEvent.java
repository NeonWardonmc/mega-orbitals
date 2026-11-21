package dev.megaorbitals.api;

import dev.megaorbitals.Cannon;
import dev.megaorbitals.Strike;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * A strike's warning has finished and it is about to land. Cancel it to stop the cannon's effect
 * (the warning has already been shown). Called for every strike that goes through MegaOrbitals.fireAt:
 * normal fires, MegaOrbitalsAPI.fireAt, Roulette, and Omega when its include-warnings setting is on.
 *
 * Cannons that implement {@link dev.megaorbitals.StrikeInterceptor} (such as the Orbital Defence Grid) are
 * asked after this event, and only if it was not cancelled.
 */
public final class OrbitalStrikeEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Strike strike;
    private boolean cancelled;

    public OrbitalStrikeEvent(Strike strike) {
        this.strike = strike;
    }

    /** The strike that is landing. */
    public Strike getStrike() { return strike; }

    /** Shortcut for getStrike().cannon. */
    public Cannon getCannon() { return strike.cannon; }

    /** Where it lands: the top centre of the targeted block (a copy). */
    public Location getLocation() { return strike.at(); }

    /** Who fired it, or null (for example a cannon fired from the console by another plugin). */
    public Player getShooter() { return strike.shooter; }

    @Override public boolean isCancelled() { return cancelled; }
    @Override public void setCancelled(boolean cancel) { this.cancelled = cancel; }

    @Override public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
