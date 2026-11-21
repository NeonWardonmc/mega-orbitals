package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;

/** Banishes every player nearby to the matching spot in the Nether. */
public final class BanishNether extends Cannon {
    public BanishNether() {
        super("&4&lBanish to the Nether", Material.NETHERRACK, "Banishes every player nearby to the", "matching spot in the Nether.");
        warning(40, "#A02010");
        noBlockDamage();
        option("radius", 15, "Range.");
    }

    @Override
    public void strike(Strike s) {
        World nether = null;
        for (World w : Bukkit.getWorlds()) if (w.getEnvironment() == World.Environment.NETHER) nether = w;
        if (nether == null) {
            if (s.shooter != null) s.actionBar(s.shooter, "&cThis server has no Nether world.");
            return;
        }
        World n = nether;
        for (Player p : s.players(s.at(), d("radius"))) {
            int x = p.getLocation().getBlockX() / 8, z = p.getLocation().getBlockZ() / 8;
            Fx.p(s.world, Particle.PORTAL, p.getLocation(), 80, 0.5, 1);
            n.getChunkAtAsync(x >> 4, z >> 4).thenAccept(chunk -> {
                int y = 64;
                for (int yy = 110; yy > 10; yy--) {
                    Block b = n.getBlockAt(x, yy, z);
                    if (b.getType().isSolid() && n.getBlockAt(x, yy + 1, z).getType().isAir() && n.getBlockAt(x, yy + 2, z).getType().isAir()) { y = yy + 1; break; }
                }
                if (y == 64) {
                    for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) Blocks.put(n.getBlockAt(x + dx, 63, z + dz), Material.NETHERRACK);
                    for (int dy = 64; dy <= 65; dy++) Blocks.put(n.getBlockAt(x, dy, z), Material.AIR);
                }
                p.teleportAsync(new Location(n, x + 0.5, y, z + 0.5));
                Fx.title(p, "&4&lBANISHED", "&7Welcome to the Nether.", 50);
            });
        }
        s.sound(s.at(), "block.portal.travel", 6f, 1f);
    }
}
