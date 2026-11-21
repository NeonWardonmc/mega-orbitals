package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;

/** Teleports every player near the target to stand right in front of you. */
public final class Summon extends Cannon {
    public Summon() {
        super("&5&lSummoning Circle", Material.RESPAWN_ANCHOR, "Teleports every player near the target", "to stand right in front of you.");
        warning(20, "#8020C0");
        noBlockDamage();
        option("radius", 20, "Range around the target.");
        option("include-mobs", false, "Also summon mobs.");
    }

    @Override
    public void strike(Strike s) {
        Player me = s.shooter;
        if (me == null || !me.isOnline()) return;
        Location front = me.getLocation().add(me.getLocation().getDirection().setY(0).normalize().multiply(4));
        int k = 0;
        for (LivingEntity le : s.victims(s.at(), d("radius"))) {
            if (!(le instanceof Player) && !b("include-mobs")) continue;
            Location to = s.surface(front.clone().add(Math.cos(k) * 2, 0, Math.sin(k) * 2));
            to.setDirection(me.getLocation().toVector().subtract(to.toVector()));
            Fx.p(s.world, Particle.PORTAL, le.getLocation().add(0, 1, 0), 40, 0.4, 0.5);
            le.teleport(to);
            Fx.p(to.getWorld(), Particle.PORTAL, to.clone().add(0, 1, 0), 40, 0.4, 0.5);
            if (le instanceof Player p) Fx.title(p, "&5&lSUMMONED", "&7by " + me.getName(), 30);
            k++;
        }
        me.playSound(me.getLocation(), "block.respawn_anchor.charge", 2f, 0.8f);
    }
}
