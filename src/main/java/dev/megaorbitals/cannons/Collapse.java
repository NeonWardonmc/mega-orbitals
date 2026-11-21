package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.block.Block;

/** Switches on gravity for every block in range. Houses, towers and trees crumble to the ground. */
public final class Collapse extends Cannon {
    public Collapse() {
        super("&7&lStructural Collapse", Material.GRAVEL, "Switches on gravity for every block in range.", "Houses, towers and trees crumble to the ground.");
        warning(30, "#808080");
        option("radius", 10, "Range.");
        option("height", 40, "How high above the target it affects.");
        option("max-blocks", 2500, "Safety cap on how many blocks can fall.");
    }

    @Override
    public void strike(Strike s) {
        int r = i("radius"), h = i("height");
        int r2 = r * r;
        int floor = s.at().getBlockY();
        int[] count = {0};
        s.blocks(Blocks.box(s.at(0, h / 2.0, 0), r, h / 2, r, (b, dx, dy, dz) -> {
            if (count[0] >= i("max-blocks") || dx * dx + dz * dz > r2 || b.getY() < floor) return;
            if (b.getType().isAir() || b.isLiquid() || !Blocks.breakable(b)) return;
            Block below = b.getRelative(0, -1, 0);
            if (!below.getType().isAir() && !below.isLiquid() && b.getY() == floor) return;
            Material m = b.getType();
            var fb = s.world.spawn(b.getLocation().add(0.5, 0, 0.5), org.bukkit.entity.FallingBlock.class, f -> {
                f.setBlockData(b.getBlockData());
                f.setDropItem(false);
                f.setHurtEntities(true);
                f.setDamagePerBlock(1f);
                f.setMaxDamage(20);
            });
            s.tag(fb);
            b.setType(Material.AIR, false);
            count[0]++;
        }));
        s.sound(s.at(), "block.stone.break", 10f, 0.4f);
        s.later(10, () -> s.soundWorld("entity.generic.explode", 0.4f, 0.5f));
    }
}
