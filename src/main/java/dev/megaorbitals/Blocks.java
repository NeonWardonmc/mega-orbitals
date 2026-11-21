package dev.megaorbitals;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

import java.util.Arrays;

/** Block helpers and ready-made lag-safe {@link BlockJob}s. */
public final class Blocks {

    private Blocks() {}

    public interface Cell { void apply(Block b, int dx, int dy, int dz); }
    public interface Column { int apply(int x, int z, double dist); }

    /** False for bedrock, portals, barriers and other unbreakable blocks. */
    public static boolean breakable(Block b) {
        Material t = b.getType();
        return t.getHardness() >= 0 && t != Material.BEDROCK && t != Material.END_PORTAL_FRAME
                && t != Material.END_PORTAL && t != Material.NETHER_PORTAL && t != Material.BARRIER
                && t != Material.END_GATEWAY && t != Material.REINFORCED_DEEPSLATE;
    }

    public static void set(Block b, Material m) {
        if (b.getType() != m && breakable(b)) b.setType(m, false);
    }

    public static void set(Block b, BlockData d) {
        if (breakable(b)) b.setBlockData(d, false);
    }

    public static boolean loaded(World w, int x, int z) {
        return w.isChunkLoaded(x >> 4, z >> 4);
    }

    /** Ground level at l's x/z, searching 60 blocks above and below l. */
    public static Location surface(Location l) {
        World w = l.getWorld();
        int x = l.getBlockX(), z = l.getBlockZ();
        if (!loaded(w, x, z)) return l.clone();
        int start = Math.min(w.getMaxHeight() - 1, l.getBlockY() + 60);
        int end = Math.max(w.getMinHeight(), l.getBlockY() - 60);
        for (int y = start; y >= end; y--) {
            if (w.getBlockAt(x, y, z).getType().isSolid()) return new Location(w, l.getX(), y + 1, l.getZ());
        }
        return l.clone();
    }

    /** Visits every block in a box around c, top layer first. */
    public static BlockJob box(Location c, int rx, int ry, int rz, Cell cell) {
        World w = c.getWorld();
        int cx = c.getBlockX(), cy = c.getBlockY(), cz = c.getBlockZ();
        int minY = Math.max(w.getMinHeight(), cy - ry), maxY = Math.min(w.getMaxHeight() - 1, cy + ry);
        return new BlockJob() {
            int y = maxY, x = -rx, z = -rz;
            boolean finished = minY > maxY;
            @Override public int step(int budget) {
                int used = 0;
                while (!finished && used < budget) {
                    int bx = cx + x, bz = cz + z;
                    if (loaded(w, bx, bz)) cell.apply(w.getBlockAt(bx, y, bz), x, y - cy, z);
                    used++;
                    if (++z > rz) { z = -rz; if (++x > rx) { x = -rx; if (--y < minY) finished = true; } }
                }
                return used;
            }
            @Override public boolean done() { return finished; }
        };
    }

    /** Slows a job down to at most perTick units per tick (for effects that should visibly spread). */
    public static BlockJob throttle(BlockJob job, int perTick) {
        return new BlockJob() {
            @Override public int step(int budget) { return job.step(Math.min(budget, perTick)); }
            @Override public boolean done() { return job.done(); }
        };
    }

    /** Fills (or with AIR, removes) a solid sphere. */
    public static BlockJob sphere(Location c, double r, Material m) {
        int ir = (int) Math.ceil(r);
        double r2 = r * r;
        return box(c, ir, ir, ir, (b, dx, dy, dz) -> {
            if (dx * dx + dy * dy + dz * dz <= r2) set(b, m);
        });
    }

    /** A hollow sphere of the given thickness. */
    public static BlockJob shell(Location c, double r, double thickness, Material m, boolean onlyAir) {
        int ir = (int) Math.ceil(r);
        return box(c, ir, ir, ir, (b, dx, dy, dz) -> {
            double d = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (d <= r && d > r - thickness && (!onlyAir || b.getType().isAir())) set(b, m);
        });
    }

    /** Visits every x/z column within r of c, nearest columns first (looks like a spreading wave). */
    public static BlockJob columns(Location c, int r, Column col) {
        int cx = c.getBlockX(), cz = c.getBlockZ();
        int n = 0;
        int side = 2 * r + 1;
        long[] keys = new long[side * side];
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                long d2 = (long) dx * dx + (long) dz * dz;
                if (d2 > (long) r * r) continue;
                keys[n++] = (d2 << 32) | ((long) (dx + r) << 16) | (dz + r);
            }
        }
        long[] sorted = Arrays.copyOf(keys, n);
        Arrays.sort(sorted);
        return new BlockJob() {
            int idx = 0;
            @Override public int step(int budget) {
                int used = 0;
                while (idx < sorted.length && used < budget) {
                    long k = sorted[idx++];
                    int dx = (int) ((k >> 16) & 0xFFFF) - r, dz = (int) (k & 0xFFFF) - r;
                    double dist = Math.sqrt((double) (k >>> 32));
                    used += Math.max(1, col.apply(cx + dx, cz + dz, dist));
                }
                return used;
            }
            @Override public boolean done() { return idx >= sorted.length; }
        };
    }

    /** Replaces the top solid block of each column within r with m. */
    public static BlockJob surface(Location c, int r, Material m) {
        World w = c.getWorld();
        return columns(c, r, (x, z, d) -> {
            if (!loaded(w, x, z)) return 1;
            Location top = surface(new Location(w, x + 0.5, c.getY(), z + 0.5));
            set(top.getBlock().getRelative(0, -1, 0), m);
            return 4;
        });
    }
}
