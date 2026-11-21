package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;

/** A satellite locks onto the nearest player and keeps striking them wherever they run. */
public final class Satellite extends Cannon {
    public Satellite() {
        super("&b&lSatellite Lock-On", Material.COMPASS, "Locks onto the nearest player and", "keeps striking them wherever they run.");
        warning(20, "#00C0FF");
        option("lock-range", 100, "How far from the target to look for a player.");
        option("strikes", 6, "Number of strikes.");
        option("seconds-between", 3, "Seconds between strikes.");
        option("power", 4.0, "Power of each strike.");
    }

    @Override
    public void strike(Strike s) {
        Player victim = s.nearestPlayer(s.at(), d("lock-range"));
        if (victim == null) {
            if (s.shooter != null) s.actionBar(s.shooter, "&cSatellite found no one to lock onto.");
            return;
        }
        if (s.shooter != null) s.actionBar(s.shooter, "&bSatellite locked onto &f" + victim.getName());
        Fx.title(victim, "&c&l⚠ LOCKED ON ⚠", "&7A satellite is tracking you. Run.", 40);
        Color cyan = Color.fromRGB(0, 200, 255);
        int gap = i("seconds-between") * 20;
        for (int k = 0; k < i("strikes"); k++) {
            s.later((long) k * gap, () -> {
                if (!victim.isOnline() || victim.isDead() || victim.getWorld() != s.world) return;
                s.every(1, 30, t -> {
                    Location l = victim.getLocation();
                    Fx.dustRing(s.world, l.clone().add(0, 0.2, 0), 3 * (1 - t / 30.0) + 0.5, cyan, 1.5f);
                    if (t % 3 == 0) Fx.dustLine(s.world, l.clone().add(0, 40, 0), l, cyan, 1f, 1.5);
                    if (t % 10 == 0) victim.playSound(l, "block.note_block.bit", 1f, 2f);
                    if (t == 29) {
                        s.world.strikeLightningEffect(l);
                        s.explode(l, (float) d("power"), false);
                    }
                });
            });
        }
    }
}
