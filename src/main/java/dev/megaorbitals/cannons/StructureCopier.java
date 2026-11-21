package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

/** Scans the area around you and beams an exact copy of it down onto the target. */
public final class StructureCopier extends Cannon {
    public StructureCopier() {
        super("&a&lStructure Copier", Material.STRUCTURE_BLOCK, "Scans the area around you and beams an exact", "copy of it down onto the target.");
        warning(30, "#40E080");
        option("radius", 8, "Half-width of the copied area (a square around the shooter).");
        option("height", 14, "How many blocks above the shooter's feet are copied.");
        option("depth", 2, "How many blocks below the shooter's feet are copied.");
        option("copy-air", true, "Also copy empty space (clears whatever is at the target so the copy is exact).");
        option("speed", 800, "Blocks pasted per tick.");
    }

    @Override
    public void strike(Strike s) {
        if (!s.breaks()) { s.tell("&cStructure Copier needs break-blocks: true."); return; }
        if (s.shooter == null || !s.shooter.isOnline()) return;
        Location src = s.shooter.getLocation();
        World sw = src.getWorld();
        int r = Math.max(1, Math.min(24, i("radius"))), up = Math.max(1, Math.min(48, i("height"))), down = Math.max(0, Math.min(16, i("depth")));
        int sx = src.getBlockX(), sy = src.getBlockY(), sz = src.getBlockZ();
        int side = 2 * r + 1, h = up + down + 1;
        BlockData[] snap = new BlockData[side * side * h];
        for (int x = -r; x <= r; x++) for (int z = -r; z <= r; z++) {
            if (!Blocks.loaded(sw, sx + x, sz + z)) continue;
            for (int y = 0; y < h; y++) {
                int wy = sy - down + y;
                if (wy < sw.getMinHeight() || wy >= sw.getMaxHeight()) continue;
                Block b = sw.getBlockAt(sx + x, wy, sz + z);
                if (!Blocks.breakable(b)) continue;
                snap[((x + r) * side + (z + r)) * h + y] = b.getBlockData();
            }
        }
        Fx.dustLine(sw, src.clone().add(0, 0.2, 0), src.clone().add(0, 30, 0), Color.LIME, 1.5f, 0.8);
        Fx.dustRing(sw, src.clone().add(0, 0.3, 0), r, Color.LIME, 1.5f);
        s.sound(src, "block.beacon.power_select", 3f, 1.6f);
        World w = s.world;
        int tx = s.at().getBlockX(), ty = s.at().getBlockY(), tz = s.at().getBlockZ();
        boolean copyAir = b("copy-air");
        s.blocks(Blocks.throttle(new BlockJob() {
            int idx = 0;
            @Override public int step(int budget) {
                int used = 0;
                while (idx < snap.length && used < budget) {
                    int n = idx++;
                    BlockData d = snap[n];
                    if (d == null) continue;
                    int y = n % h, col = n / h, x = col / side - r, z = col % side - r;
                    if (d.getMaterial().isAir() && (!copyAir || y < down)) continue;
                    int wy = ty - down + y;
                    if (wy < w.getMinHeight() || wy >= w.getMaxHeight() || !Blocks.loaded(w, tx + x, tz + z)) continue;
                    Block b = w.getBlockAt(tx + x, wy, tz + z);
                    used++;
                    if (!b.getBlockData().matches(d)) Blocks.set(b, d);
                }
                return Math.max(1, used);
            }
            @Override public boolean done() { return idx >= snap.length; }
        }, Math.max(50, i("speed"))));
        Fx.p(w, Particle.END_ROD, s.at(0, 4, 0), 150, r / 2.0, 0.05);
        s.sound(s.at(), "block.beacon.activate", 5f, 1.4f);
    }
}
