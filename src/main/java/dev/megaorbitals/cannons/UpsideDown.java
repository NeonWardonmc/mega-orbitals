package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.TileState;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.Slab;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/** Flips a whole chunk of the world upside down: the ground becomes a ceiling and buildings hang from the sky. */
public final class UpsideDown extends Cannon {
    public UpsideDown() {
        super("&5&lUpside-Down", Material.POINTED_DRIPSTONE, "Flips the terrain upside down: the ground", "becomes a ceiling and buildings hang from the sky.");
        warning(40, "#8040C0");
        option("radius", 10, "Radius of the flipped area.");
        option("depth", 6, "How many blocks below the surface are flipped too (they become the ceiling).");
        option("height", 24, "How many blocks above the surface are flipped.");
        option("speed", 1500, "Blocks changed per tick.");
    }

    @Override
    public void strike(Strike s) {
        if (!s.breaks()) { s.tell("&cUpside-Down needs break-blocks: true."); return; }
        World w = s.world;
        int r = Math.max(2, Math.min(32, i("radius")));
        int cx = s.at().getBlockX(), cz = s.at().getBlockZ();
        int y0 = Math.max(w.getMinHeight(), s.at().getBlockY() - Math.max(1, i("depth")));
        int y1 = Math.min(w.getMaxHeight() - 1, s.at().getBlockY() + Math.max(4, i("height")));
        int side = 2 * r + 1, h = y1 - y0 + 1;
        BlockData[] snap = new BlockData[side * side * h];
        boolean[] fixed = new boolean[snap.length];
        for (int x = -r; x <= r; x++) for (int z = -r; z <= r; z++) {
            if (x * x + z * z > r * r || !Blocks.loaded(w, cx + x, cz + z)) continue;
            for (int y = 0; y < h; y++) {
                Block b = w.getBlockAt(cx + x, y0 + y, cz + z);
                int idx = ((x + r) * side + (z + r)) * h + y;
                snap[idx] = b.getBlockData();
                fixed[idx] = !Blocks.breakable(b) || b.getState(false) instanceof TileState;
            }
        }
        Fx.p(w, Particle.REVERSE_PORTAL, s.at(0, 3, 0), 300, r / 2.0, 0.5);
        s.sound(s.at(), "block.end_portal.spawn", 6f, 0.5f);
        for (Player p : s.players(s.at(), r + 2)) p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 200, 0));
        s.blocks(Blocks.throttle(new BlockJob() {
            int idx = 0;
            @Override public int step(int budget) {
                int used = 0;
                while (idx < snap.length && used < budget) {
                    int i = idx++;
                    BlockData from = snap[i];
                    if (from == null) continue;
                    int y = i % h, col = i / h;
                    int other = col * h + (h - 1 - y);
                    used++;
                    if (fixed[i] || fixed[other] || snap[other] == null) continue;
                    BlockData src = snap[other];
                    if (src.matches(from)) continue;
                    int x = col / side - r, z = col % side - r;
                    Blocks.put(w.getBlockAt(cx + x, y0 + y, cz + z), flip(src), false);
                }
                return Math.max(1, used);
            }
            @Override public boolean done() { return idx >= snap.length; }
        }, Math.max(100, i("speed"))));
        s.later(Math.max(10, snap.length / Math.max(100, i("speed"))), () -> s.sound(s.at(), "entity.elder_guardian.curse", 4f, 0.6f));
    }

    private static BlockData flip(BlockData d) {
        BlockData c = d.clone();
        if (c instanceof Slab sl && sl.getType() != Slab.Type.DOUBLE) sl.setType(sl.getType() == Slab.Type.TOP ? Slab.Type.BOTTOM : Slab.Type.TOP);
        else if (c instanceof Bisected bi) bi.setHalf(bi.getHalf() == Bisected.Half.TOP ? Bisected.Half.BOTTOM : Bisected.Half.TOP);
        if (c instanceof Directional dir) {
            BlockFace f = dir.getFacing();
            if ((f == BlockFace.UP || f == BlockFace.DOWN) && dir.getFaces().contains(f.getOppositeFace())) dir.setFacing(f.getOppositeFace());
        }
        return c;
    }
}
