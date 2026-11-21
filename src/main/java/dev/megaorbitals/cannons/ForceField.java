package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import java.util.*;

/** Traps every target inside an invisible, unbreakable force-field box for a while. */
public final class ForceField extends Cannon {
    public ForceField() {
        super("&b&lForce Field Cells", Material.BARRIER, "Traps every target inside an invisible,", "unbreakable force-field box for a while.");
        warning(15, "#80E0FF");
        option("radius", 20, "Range.");
        option("seconds", 15, "How long the cells last.");
    }

    @Override
    public void strike(Strike s) {
        if (!s.breaks()) return;
        List<Block> walls = new ArrayList<>();
        List<Location> cells = new ArrayList<>();
        for (LivingEntity le : s.victims(s.at(), d("radius"))) {
            Location base = le.getLocation().getBlock().getLocation();
            le.teleport(base.clone().add(0.5, 0, 0.5).setDirection(le.getLocation().getDirection()));
            cells.add(base);
            for (int x = -1; x <= 1; x++) for (int y = -1; y <= 2; y++) for (int z = -1; z <= 1; z++) {
                if (x == 0 && z == 0 && (y == 0 || y == 1)) continue;
                Block b = base.clone().add(x, y, z).getBlock();
                if (b.getType().isAir()) { Blocks.put(b, Material.BARRIER, false); walls.add(b); }
            }
        }
        Color c = Color.fromRGB(120, 220, 255);
        s.every(5, i("seconds") * 4, t -> {
            for (Location l : cells) Fx.dustSphere(s.world, l.clone().add(0.5, 1, 0.5), 1.6, c, 0.7f, 30);
        });
        s.later(i("seconds") * 20L, () -> {
            for (Block b : walls) if (b.getType() == Material.BARRIER) Blocks.put(b, Material.AIR, false);
            s.sound(s.at(), "block.beacon.deactivate", 6f, 1.5f);
        });
        s.sound(s.at(), "block.beacon.activate", 6f, 1.5f);
    }
}
