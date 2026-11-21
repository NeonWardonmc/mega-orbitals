package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;

/** A holy light that wipes every hostile mob in range off the map. Players are untouched. */
public final class Purge extends Cannon {
    public Purge() {
        super("&f&lPurge", Material.NETHER_STAR, "A holy light that wipes every hostile", "mob in range off the map. Players are safe.");
        warning(20, "#FFFFE0");
        noBlockDamage();
        option("radius", 64, "Range.");
        option("include-bosses", false, "Also remove withers, wardens and elder guardians.");
    }

    @Override
    public void strike(Strike s) {
        double r = d("radius");
        int count = 0;
        for (Entity e : s.world.getNearbyEntities(s.at(), r, r, r)) {
            boolean hostile = e instanceof Monster || e instanceof Slime || e instanceof Phantom || e instanceof Ghast || e instanceof Shulker;
            boolean boss = e instanceof Wither || e instanceof Warden || e instanceof ElderGuardian || e instanceof EnderDragon;
            if (!hostile || (boss && !b("include-bosses"))) continue;
            Fx.p(s.world, Particle.END_ROD, e.getLocation().add(0, 1, 0), 15, 0.4, 0.1);
            e.remove();
            count++;
        }
        Fx.dustLine(s.world, s.at(0, 80, 0), s.at(), Color.WHITE, 4f, 1);
        Fx.ring(s.world, Particle.END_ROD, s.at(0, 1, 0), Math.min(r, 30), 120);
        s.sound(s.at(), "block.beacon.activate", 10f, 2f);
        if (s.shooter != null) s.actionBar(s.shooter, "&fPurged &e" + count + " &fhostile mobs.");
    }
}
