package dev.megaorbitals;

/** A big block edit that is spread over several ticks to avoid lag. */
public interface BlockJob {
    /** Do up to {@code budget} units of work. Return how many units were used. */
    int step(int budget);

    /** True once the job is finished. */
    boolean done();
}
