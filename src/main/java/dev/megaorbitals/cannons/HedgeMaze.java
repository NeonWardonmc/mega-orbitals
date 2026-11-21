package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.block.data.type.Leaves;
import java.util.*;

/** A hedge maze grows instantly around the target. Everyone inside has to find the way out. */
public final class HedgeMaze extends Cannon {
    public HedgeMaze() {
        super("&2&lHedge Maze", Material.OAK_LEAVES, "A hedge maze grows instantly around the target.", "Everyone inside has to find the way out.");
        warning(15, "#30A030");
        option("cells", 12, "Maze size in cells (each cell is 2 blocks).");
        option("wall-height", 4, "Height of the hedges.");
    }

    @Override
    public void strike(Strike s) {
        if (!s.breaks()) return;
        int n = i("cells"), h = i("wall-height");
        int size = n * 2 + 1;
        boolean[][] open = new boolean[size][size];
        Deque<int[]> stack = new ArrayDeque<>();
        Random rnd = new Random();
        boolean[][] seen = new boolean[n][n];
        stack.push(new int[]{0, 0});
        seen[0][0] = true;
        open[1][1] = true;
        int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        while (!stack.isEmpty()) {
            int[] cur = stack.peek();
            List<int[]> next = new ArrayList<>();
            for (int[] d : dirs) {
                int nx = cur[0] + d[0], nz = cur[1] + d[1];
                if (nx >= 0 && nz >= 0 && nx < n && nz < n && !seen[nx][nz]) next.add(new int[]{nx, nz, d[0], d[1]});
            }
            if (next.isEmpty()) { stack.pop(); continue; }
            int[] pick = next.get(rnd.nextInt(next.size()));
            seen[pick[0]][pick[1]] = true;
            open[cur[0] * 2 + 1 + pick[2]][cur[1] * 2 + 1 + pick[3]] = true;
            open[pick[0] * 2 + 1][pick[1] * 2 + 1] = true;
            stack.push(new int[]{pick[0], pick[1]});
        }
        open[0][1] = true;
        open[size - 1][size - 2] = true;
        Leaves leaves = (Leaves) Material.OAK_LEAVES.createBlockData();
        leaves.setPersistent(true);
        int off = size / 2;
        for (int x = 0; x < size; x++) for (int z = 0; z < size; z++) {
            Location g = s.surface(s.at(x - off, 0, z - off));
            for (int y = 0; y < h; y++) {
                var b = g.clone().add(0, y, 0).getBlock();
                if (open[x][z]) Blocks.set(b, Material.AIR);
                else Blocks.set(b, leaves);
            }
        }
        s.sound(s.at(), "block.grass.place", 10f, 0.6f);
    }
}
