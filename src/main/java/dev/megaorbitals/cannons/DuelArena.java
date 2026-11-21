package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import java.util.*;

/** Grabs the two players nearest the target and locks them in a walled arena to fight it out. */
public final class DuelArena extends Cannon {
    public DuelArena() {
        super("&c&lDuel Arena", Material.IRON_SWORD, "Grabs the two players nearest the target", "and locks them in an arena to fight it out.");
        warning(20, "#C04040");
        option("range", 40, "How far from the target to find duellists.");
        option("arena-radius", 12, "Arena size.");
        option("seconds", 90, "How long the walls stand.");
    }

    @Override
    public void strike(Strike s) {
        List<Player> ps = s.players(s.at(), d("range"));
        ps.sort(Comparator.comparingDouble(p -> p.getLocation().distanceSquared(s.at())));
        if (s.shooter != null && ps.size() < 2 && s.shooter.getWorld() == s.world && !ps.contains(s.shooter)) ps.add(s.shooter);
        if (ps.size() < 2) {
            if (s.shooter != null) s.actionBar(s.shooter, "&cNeed two players near the target for a duel.");
            return;
        }
        int r = i("arena-radius");
        Location c = s.at();
        List<Block> placed = new ArrayList<>();
        if (s.breaks()) {
            for (int x = -r; x <= r; x++) for (int z = -r; z <= r; z++) {
                double dd = Math.sqrt(x * x + z * z);
                if (dd > r) continue;
                for (int y = -1; y <= 6; y++) {
                    Block b = c.clone().add(x, y, z).getBlock();
                    Material m = y == -1 ? Material.SMOOTH_STONE : (dd > r - 1 ? Material.RED_STAINED_GLASS : Material.AIR);
                    if (y == 6 && dd <= r) m = Material.BARRIER;
                    if (m != Material.AIR || !b.getType().isAir()) { Blocks.set(b, m); if (m != Material.SMOOTH_STONE) placed.add(b); }
                }
            }
        }
        Player a = ps.get(0), b = ps.get(1);
        Location la = c.clone().add(r - 3, 0, 0), lb = c.clone().add(-(r - 3), 0, 0);
        la.setDirection(lb.toVector().subtract(la.toVector()));
        lb.setDirection(la.toVector().subtract(lb.toVector()));
        a.teleport(la);
        b.teleport(lb);
        Fx.title(a, "&c&lDUEL!", "&7vs " + b.getName(), 50);
        Fx.title(b, "&c&lDUEL!", "&7vs " + a.getName(), 50);
        s.sound(c, "event.raid.horn", 6f, 1.5f);
        s.later(i("seconds") * 20L, () -> {
            for (Block bl : placed) if (bl.getType() == Material.RED_STAINED_GLASS || bl.getType() == Material.BARRIER) Blocks.put(bl, Material.AIR, false);
        });
    }
}
