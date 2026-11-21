package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;

/** Battle-royale storm: a deadly ring closes in on the target. Anyone left outside takes damage. */
public final class ShrinkingZone extends Cannon {
    public ShrinkingZone() {
        super("&5&lThe Storm", Material.PURPLE_STAINED_GLASS, "Battle-royale storm: a deadly ring closes in.", "Anyone left outside it takes damage.");
        warning(20, "#8020C0");
        noBlockDamage();
        option("start-radius", 60, "Starting size of the safe zone.");
        option("end-radius", 5, "Final size of the safe zone.");
        option("seconds", 90, "How long the storm takes to close.");
        option("damage", 2.0, "Damage per second outside the zone.");
    }

    @Override
    public void strike(Strike s) {
        int ticks = i("seconds") * 20;
        Color purple = Color.fromRGB(150, 40, 220);
        s.every(10, ticks / 10 + 20, t -> {
            double r = Math.max(d("end-radius"), d("start-radius") - (d("start-radius") - d("end-radius")) * (t * 10.0) / ticks);
            for (double y = 0; y < 12; y += 3) Fx.dustRing(s.world, s.at(0, y, 0), r, purple, 2f);
            if (t % 2 != 0) return;
            for (Player p : s.world.getPlayers()) {
                if (s.spared(p)) continue;
                double dist = Math.hypot(p.getLocation().getX() - s.at().getX(), p.getLocation().getZ() - s.at().getZ());
                if (dist > r && dist < d("start-radius") * 2) {
                    s.damage(p, d("damage"));
                    s.actionBar(p, "&5&lYOU ARE IN THE STORM &7- get to the centre!");
                }
            }
        });
    }
}
