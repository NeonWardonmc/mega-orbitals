package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Ladder;

/** Raises a stone watchtower with a ladder and a lookout platform. Optionally puts you on top. */
public final class Watchtower extends Cannon {
    public Watchtower() {
        super("&7&lWatchtower", Material.LADDER, "Raises a stone watchtower with a ladder", "and a lookout platform.");
        warning(10, "#909090");
        option("height", 20, "Tower height.");
        option("teleport-shooter", true, "Put you on top when it's built.");
    }

    @Override
    public void strike(Strike s) {
        if (!s.breaks()) return;
        int h = i("height");
        Location base = s.at();
        s.every(1, h + 2, y -> {
            for (int x = -2; x <= 2; x++) for (int z = -2; z <= 2; z++) {
                boolean wall = Math.abs(x) == 2 || Math.abs(z) == 2;
                var b = base.clone().add(x, y, z).getBlock();
                if (y < h) {
                    if (wall) Blocks.set(b, (x == 0 && z == -2 && y < 2) ? Material.AIR : Material.STONE_BRICKS);
                    else Blocks.set(b, Material.AIR);
                } else if (y == h) Blocks.set(b, Material.SPRUCE_PLANKS);
                else if (wall) Blocks.set(b, Material.SPRUCE_FENCE);
                else Blocks.set(b, Material.AIR);
            }
            if (y < h) {
                var ladder = base.clone().add(0, y, 1).getBlock();
                ladder.setType(Material.LADDER, false);
                if (ladder.getBlockData() instanceof Ladder l) { l.setFacing(BlockFace.NORTH); ladder.setBlockData(l, false); }
            }
            if (y == h) Blocks.set(base.clone().add(0, h, 1).getBlock(), Material.AIR);
        });
        s.later(h + 3L, () -> {
            if (b("teleport-shooter") && s.shooter != null) s.shooter.teleport(base.clone().add(1, h + 1, 1).setDirection(s.shooter.getLocation().getDirection()));
            s.sound(base, "block.wood.place", 6f, 1f);
        });
    }
}
