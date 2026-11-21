package dev.megaorbitals;

/**
 * A cannon that implements this is asked, just before any strike lands, whether it wants to stop it
 * (used by the Orbital Defence Grid). Return true to cancel the strike.
 */
public interface StrikeInterceptor {
    boolean intercept(Strike incoming);
}
