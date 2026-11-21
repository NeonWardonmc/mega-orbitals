package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Orientable;
import org.bukkit.entity.*;

/** Builds a lit Nether portal around every target, frozen in place. Next stop: the Nether. */
public final class PortalTrap extends Cannon {
    public PortalTrap() {
        super("&5&lPortal Trap", Material.CRYING_OBSIDIAN, "Builds a lit Nether portal around every target", "and holds them in it. Next stop: the Nether.");
        warning(20, "#8000C0");
        option("radius", 15, "Range.");
        option("players-only", true, "Only trap players.");
    }

    @Override
    public void strike(Strike s) {
        if (!s.breaks()) return;
        for (LivingEntity le : s.victims(s.at(), d("radius"))) {
            if (b("players-only") && !(le instanceof Player)) continue;
            Location base = le.getLocation().getBlock().getLocation();
            for (int x = -1; x <= 2; x++) for (int y = -1; y <= 3; y++) {
                Block b = base.clone().add(x, y, 0).getBlock();
                boolean frame = x == -1 || x == 2 || y == -1 || y == 3;
                if (frame) Blocks.set(b, Material.OBSIDIAN);
                else {
                    Blocks.set(b, Material.AIR);
                    b.setType(Material.NETHER_PORTAL, false);
                    if (b.getBlockData() instanceof Orientable o) { o.setAxis(Axis.X); b.setBlockData(o, false); }
                }
            }
            Location hold = base.clone().add(0.5, 0, 0.5);
            hold.setDirection(le.getLocation().getDirection());
            s.every(1, 100, t -> { if (le.isValid() && le.getLocation().distanceSquared(hold) > 0.3) le.teleport(hold); });
            if (le instanceof Player p) Fx.title(p, "&5&lPORTAL TRAP", "&7Enjoy the Nether.", 40);
        }
        s.sound(s.at(), "block.portal.trigger", 8f, 1f);
    }
}
