package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;

/** Banishes every player nearby to the End, onto the obsidian platform. The dragon is waiting. */
public final class BanishEnd extends Cannon {
    public BanishEnd() {
        super("&e&lBanish to the End", Material.END_PORTAL_FRAME, "Banishes every player nearby to the End,", "onto the obsidian platform. Say hi to the dragon.");
        warning(40, "#E0E0A0");
        noBlockDamage();
        option("radius", 15, "Range.");
    }

    @Override
    public void strike(Strike s) {
        World end = null;
        for (World w : Bukkit.getWorlds()) if (w.getEnvironment() == World.Environment.THE_END) end = w;
        if (end == null) {
            if (s.shooter != null) s.actionBar(s.shooter, "&cThis server has no End world.");
            return;
        }
        Location plat = new Location(end, 100.5, 49, 0.5);
        for (int x = -2; x <= 2; x++) for (int z = -2; z <= 2; z++) {
            end.getBlockAt(100 + x, 48, z).setType(Material.OBSIDIAN);
            for (int y = 49; y <= 51; y++) end.getBlockAt(100 + x, y, z).setType(Material.AIR);
        }
        for (Player p : s.players(s.at(), d("radius"))) {
            Fx.p(s.world, Particle.PORTAL, p.getLocation(), 80, 0.5, 1);
            p.teleport(plat);
            Fx.title(p, "&e&lBANISHED", "&7Welcome to the End.", 50);
        }
        s.sound(s.at(), "block.end_portal.spawn", 8f, 1f);
    }
}
