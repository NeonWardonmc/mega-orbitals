package dev.megaorbitals;

import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Runs {@link BlockJob}s a little every tick, sharing a per-tick budget between them. */
public final class BlockQueue extends BukkitRunnable {

    private final MegaOrbitals plugin;
    private final List<BlockJob> jobs = new ArrayList<>();

    public BlockQueue(MegaOrbitals plugin) {
        this.plugin = plugin;
    }

    public void submit(BlockJob job) {
        jobs.add(job);
    }

    public int size() { return jobs.size(); }

    @Override
    public void run() {
        if (jobs.isEmpty()) return;
        int budget = Math.max(500, plugin.settings().blocksPerTick());
        int share = Math.max(200, budget / jobs.size());
        Iterator<BlockJob> it = jobs.iterator();
        while (it.hasNext()) {
            BlockJob job = it.next();
            try {
                job.step(share);
                if (job.done()) it.remove();
            } catch (Exception e) {
                plugin.getLogger().warning("Block job failed: " + e);
                it.remove();
            }
        }
    }
}
