package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import java.util.*;

/** Locks every player nearby in a cage high in the sky. When the time is up, the floor opens. */
public final class SkyJail extends Cannon {
    public SkyJail() {
        super("&7&lSky Jail", Material.IRON_BARS, "Locks every player nearby in a cage high in", "the sky. When time's up, the floor opens.");
        warning(20, "#A0A0A0");
        option("radius", 20, "Range.");
        option("height", 100, "How high the cages are.");
        option("jail-seconds", 30, "Time before the floor opens.");
        option("open-floor", true, "Drop them when time is up (false = cage stays).");
    }

    @Override
    public void strike(Strike s) {
        if (!s.breaks()) return;
        List<Block> floors = new ArrayList<>();
        for (Player p : s.players(s.at(), d("radius"))) {
            Location base = p.getLocation().getBlock().getLocation().add(0, i("height"), 0);
            if (base.getY() > s.world.getMaxHeight() - 6) base.setY(s.world.getMaxHeight() - 6);
            for (int x = -2; x <= 2; x++) for (int y = -1; y <= 3; y++) for (int z = -2; z <= 2; z++) {
                Block b = base.clone().add(x, y, z).getBlock();
                boolean wall = Math.abs(x) == 2 || Math.abs(z) == 2;
                if (y == -1) { b.setType(Material.GLASS, false); floors.add(b); }
                else if (y == 3) b.setType(Material.IRON_BLOCK, false);
                else if (wall) b.setType(Material.IRON_BARS, true);
                else b.setType(Material.AIR, false);
            }
            p.teleport(base.clone().add(0.5, 0, 0.5).setDirection(p.getLocation().getDirection()));
            Fx.title(p, "&7&lSKY JAIL", "&8Sentence: " + i("jail-seconds") + " seconds.", 40);
        }
        if (b("open-floor")) {
            s.later(i("jail-seconds") * 20L, () -> {
                for (Block b : floors) if (b.getType() == Material.GLASS) b.setType(Material.AIR, false);
                s.sound(s.at(), "block.glass.break", 4f, 0.6f);
            });
        }
    }
}
