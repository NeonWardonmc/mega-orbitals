package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;

/** You swap places with the player nearest the target. */
public final class Switcheroo extends Cannon {
    public Switcheroo() {
        super("&d&lSwitcheroo", Material.ENDER_PEARL, "You swap places with the player", "nearest the target.");
        warning(5, "#E060FF");
        noBlockDamage();
        option("range", 30, "How far from the target to find someone.");
        option("mobs-too", true, "Swap with a mob if no player is near.");
    }

    @Override
    public void strike(Strike s) {
        Player me = s.shooter;
        if (me == null || !me.isOnline()) return;
        LivingEntity other = s.nearestPlayer(s.at(), d("range"));
        if (other == null && b("mobs-too")) other = s.nearest(s.at(), d("range"));
        if (other == null) {
            s.actionBar(me, "&cNo one to swap with.");
            return;
        }
        Location a = me.getLocation(), b = other.getLocation();
        a.setDirection(b.getDirection());
        Location bMe = b.clone();
        bMe.setYaw(me.getLocation().getYaw());
        bMe.setPitch(me.getLocation().getPitch());
        Fx.p(s.world, Particle.REVERSE_PORTAL, b.clone().add(0, 1, 0), 40, 0.4, 0.1);
        Fx.p(me.getWorld(), Particle.REVERSE_PORTAL, a.clone().add(0, 1, 0), 40, 0.4, 0.1);
        other.teleport(a);
        me.teleport(bMe);
        s.sound(b, "entity.enderman.teleport", 3f, 1f);
        if (other instanceof Player op) Fx.title(op, "&d&lSWITCHEROO", "&7You swapped places with " + me.getName(), 30);
    }
}
