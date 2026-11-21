package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;

/** Opens a gate to the void beneath the world. Everyone caught in it drops out of existence. */
public final class VoidGate extends Cannon {
    public VoidGate() {
        super("&0&lVoid Gate", Material.BLACK_STAINED_GLASS, "Opens a gate to the void beneath the world.", "Everyone caught in it falls out of existence.");
        warning(60, "#000000");
        noBlockDamage();
        option("radius", 8, "Size of the gate.");
        option("players-only", false, "Only send players into the void.");
    }

    @Override
    public void warn(Strike s) {
        super.warn(s);
        s.every(2, warningTicks() / 2, t -> Fx.dustRing(s.world, s.at(0, 0.1, 0), d("radius") * Math.random(), Color.BLACK, 3f));
    }

    @Override
    public void strike(Strike s) {
        for (LivingEntity le : s.victims(s.at(), d("radius"))) {
            if (b("players-only") && !(le instanceof Player)) continue;
            Fx.p(s.world, Particle.REVERSE_PORTAL, le.getLocation().add(0, 1, 0), 60, 0.4, 0.2);
            Location to = le.getLocation();
            to.setY(s.world.getMinHeight() - 30);
            le.teleport(to);
            if (le instanceof Player p) Fx.title(p, "&0&l▼", "&8The void takes you.", 40);
        }
        s.sound(s.at(), "block.end_portal.spawn", 10f, 0.3f);
    }
}
