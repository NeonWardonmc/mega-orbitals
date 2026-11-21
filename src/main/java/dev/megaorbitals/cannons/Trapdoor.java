package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;

/** The floor vanishes under every target, dropping them down a deep shaft. */
public final class Trapdoor extends Cannon {
    public Trapdoor() {
        super("&8&lTrapdoor", Material.IRON_TRAPDOOR, "The floor vanishes under every target,", "dropping them down a deep shaft.");
        warning(10, "#404040");
        option("radius", 25, "Range.");
        option("depth", 35, "Depth of each shaft.");
    }

    @Override
    public void strike(Strike s) {
        if (!s.breaks()) return;
        for (LivingEntity le : s.victims(s.at(), d("radius"))) {
            Location base = le.getLocation().getBlock().getLocation();
            for (int y = 1; y <= i("depth"); y++) {
                if (base.getBlockY() - y <= s.world.getMinHeight() + 1) break;
                for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++) Blocks.set(base.clone().add(x, -y, z).getBlock(), Material.AIR);
            }
            s.sound(base, "block.iron_trapdoor.open", 2f, 0.5f);
        }
    }
}
