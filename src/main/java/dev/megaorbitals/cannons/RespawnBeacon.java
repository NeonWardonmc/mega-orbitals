package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;

/** Sets your respawn point to the target, marked by a light beam. Die anywhere, come back here. */
public final class RespawnBeacon extends Cannon {
    public RespawnBeacon() {
        super("&b&lRespawn Beacon", Material.RESPAWN_ANCHOR, "Sets your respawn point to the target.", "Die anywhere, come back here.");
        warning(10, "#60C0FF");
        noBlockDamage();
    }

    @Override
    public void strike(Strike s) {
        if (s.shooter == null) return;
        s.shooter.setRespawnLocation(s.at(), true);
        s.every(4, 30, t -> Fx.dustLine(s.world, s.at(0, 60, 0), s.at(), Color.fromRGB(100, 200, 255), 2f, 1));
        s.actionBar(s.shooter, "&bRespawn point set to &f" + s.at().getBlockX() + ", " + s.at().getBlockY() + ", " + s.at().getBlockZ());
        s.sound(s.at(), "block.respawn_anchor.set_spawn", 6f, 1f);
    }
}
