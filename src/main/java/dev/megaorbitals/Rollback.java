package dev.megaorbitals;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Undo system. Every block a strike changes is recorded (its original state) the first time it is touched.
 * Restoring replays the records newest-first, so each block ends up exactly as it was before the strike.
 * Records are stored compactly: packed positions plus an index into a palette of distinct block states.
 */
public final class Rollback {

    /** One strike's worth of changes (Omega and Roulette sub-strikes share their parent's record). */
    public static final class Record {
        public final int id;
        public final UUID shooter;
        public final String shooterName;
        public final String cannon;
        public final World world;
        public final long time = System.currentTimeMillis();
        private long[] pos = new long[256];
        private int[] state = new int[256];
        private int size;
        private boolean truncated;
        private boolean restored;
        private final Map<BlockData, Integer> palette = new HashMap<>();
        private final List<BlockData> paletteList = new ArrayList<>();

        Record(int id, UUID shooter, String shooterName, String cannon, World world) {
            this.id = id;
            this.shooter = shooter;
            this.shooterName = shooterName;
            this.cannon = cannon;
            this.world = world;
        }

        public int size() { return size; }
        public boolean truncated() { return truncated; }
        public boolean restored() { return restored; }

        void add(Block b, int cap) {
            if (restored || b.getWorld() != world) return;
            if (size >= cap) { truncated = true; return; }
            if (size == pos.length) {
                pos = Arrays.copyOf(pos, pos.length * 2);
                state = Arrays.copyOf(state, state.length * 2);
            }
            BlockData data = b.getBlockData();
            Integer idx = palette.get(data);
            if (idx == null) {
                idx = paletteList.size();
                palette.put(data, idx);
                paletteList.add(data);
            }
            pos[size] = pack(b.getX(), b.getY(), b.getZ());
            state[size] = idx;
            size++;
        }
    }

    private final MegaOrbitals plugin;
    private final List<Record> records = new ArrayList<>();
    private final Map<Integer, Record> byId = new HashMap<>();
    private int nextId = 1;

    public Rollback(MegaOrbitals plugin) {
        this.plugin = plugin;
        plugin.getServer().getScheduler().runTaskTimer(plugin, this::expire, 1200L, 1200L);
    }

    private boolean enabled() { return plugin.getConfig().getBoolean("rollback.enabled", true); }
    private int cap() { return Math.max(1000, plugin.getConfig().getInt("rollback.max-blocks-per-strike", 5_000_000)); }

    /** Creates the record for a new top-level strike (or null if undo is off). */
    public Record begin(Cannon cannon, World world, org.bukkit.entity.Player shooter) {
        if (!enabled()) return null;
        Record r = new Record(nextId++, shooter == null ? null : shooter.getUniqueId(),
                shooter == null ? "console" : shooter.getName(), cannon.id(), world);
        records.add(r);
        byId.put(r.id, r);
        return r;
    }

    public Record byId(int id) { return byId.get(id); }

    /** Records b's current state into the record of the strike that is running right now (if any). */
    public void record(Block b) {
        Strike s = Strike.current();
        if (s != null) record(s.record(), b);
    }

    public void record(Record r, Block b) {
        if (r != null) r.add(b, cap());
    }

    public List<Record> records() { return records; }

    /** Most recent unrestored record, optionally only from one player. */
    public Record latest(UUID shooter) {
        for (int i = records.size() - 1; i >= 0; i--) {
            Record r = records.get(i);
            if (!r.restored && r.size > 0 && (shooter == null || shooter.equals(r.shooter))) return r;
        }
        return null;
    }

    /** Puts every block back. Runs through the lag-safe block queue. */
    public void restore(Record r) {
        if (r == null || r.restored) return;
        r.restored = true;
        final int[] i = {r.size - 1};
        BlockData[] pal = r.paletteList.toArray(new BlockData[0]);
        plugin.blockQueue().submit(new BlockJob() {
            @Override public int step(int budget) {
                int used = 0;
                while (i[0] >= 0 && used < budget) {
                    long p = r.pos[i[0]];
                    r.world.getBlockAt(unpackX(p), unpackY(p), unpackZ(p)).setBlockData(pal[r.state[i[0]]], false);
                    i[0]--;
                    used++;
                }
                return used;
            }
            @Override public boolean done() {
                if (i[0] < 0) { r.pos = new long[0]; r.state = new int[0]; return true; }
                return false;
            }
        });
    }

    /** Drops records older than rollback.keep-minutes (they can no longer be undone). */
    private void expire() {
        long keep = plugin.getConfig().getLong("rollback.keep-minutes", 60) * 60_000L;
        long now = System.currentTimeMillis();
        Iterator<Record> it = records.iterator();
        while (it.hasNext()) {
            Record r = it.next();
            if (r.restored || now - r.time > keep) { it.remove(); byId.remove(r.id); }
        }
    }

    // 26 bits x, 12 bits y (offset), 26 bits z
    static long pack(int x, int y, int z) {
        return ((long) (x & 0x3FFFFFF) << 38) | ((long) ((y + 2048) & 0xFFF) << 26) | (z & 0x3FFFFFF);
    }
    static int unpackX(long p) { return (int) (p >> 38); }
    static int unpackY(long p) { return (int) ((p >> 26) & 0xFFF) - 2048; }
    static int unpackZ(long p) { return (int) (p << 38 >> 38); }
}
