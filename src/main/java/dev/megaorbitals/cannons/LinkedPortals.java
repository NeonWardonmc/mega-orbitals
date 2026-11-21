package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;
import java.util.*;

/** Opens two linked portals: one next to you and one at the target. Step through either to come out of the other. */
public final class LinkedPortals extends Cannon {
    public LinkedPortals() {
        super("&d&lLinked Portals", Material.ENDER_EYE, "Opens two linked portals: one next to you", "and one at the target. Walk through either!");
        warning(10, "#C040FF");
        noBlockDamage();
        option("seconds", 60, "How long the portals stay open.");
        option("portal-size", 1.3, "How close you must get to a portal to go through.");
        option("distance-from-shooter", 3.0, "How far in front of the shooter their portal opens.");
    }

    @Override
    public void strike(Strike s) {
        Location b = s.at();
        Location a;
        if (s.shooter != null && s.shooter.isOnline()) {
            Vector look = s.shooter.getLocation().getDirection().setY(0);
            if (look.lengthSquared() < 0.01) look = new Vector(1, 0, 0);
            a = s.surface(s.shooter.getLocation().add(look.normalize().multiply(d("distance-from-shooter"))));
        } else {
            a = s.surface(s.at(8, 0, 0));
        }
        Location[] portals = {a, b};
        Color[] cols = {Color.fromRGB(0x30A0FF), Color.fromRGB(0xFF8020)};
        Map<UUID, Integer> cooldown = new HashMap<>();
        double size = d("portal-size");
        for (Location l : portals) s.sound(l, "block.end_portal.spawn", 2f, 1.4f);
        s.every(1, i("seconds") * 20, t -> {
            for (int k = 0; k < 2; k++) {
                Location c = portals[k].clone().add(0, 1.1, 0);
                double spin = t * 0.15;
                for (int j = 0; j < 14; j++) {
                    double ang = spin + j * Math.PI * 2 / 14;
                    Fx.dust(c.getWorld(), c.clone().add(Math.cos(ang) * 0.8, Math.sin(ang) * 1.1, 0), cols[k], 1.2f, 1, 0);
                    Fx.dust(c.getWorld(), c.clone().add(0, Math.sin(ang) * 1.1, Math.cos(ang) * 0.8), cols[k], 1.2f, 1, 0);
                }
                if (t % 3 == 0) Fx.p(c.getWorld(), Particle.PORTAL, c, 6, 0.4, 0.3);
            }
            if (t % 40 == 0) for (Location l : portals) s.sound(l, "block.portal.ambient", 0.6f, 1.6f);
            for (int k = 0; k < 2; k++) {
                Location from = portals[k].clone().add(0, 1, 0), to = portals[1 - k];
                if (from.getWorld() == null || !from.isChunkLoaded()) continue;
                for (Entity e : from.getWorld().getNearbyEntities(from, size, 1.2, size)) {
                    if (e instanceof Display || e instanceof Interaction || e.isInsideVehicle()) continue;
                    Integer cd = cooldown.get(e.getUniqueId());
                    if (cd != null && t - cd < 40) continue;
                    cooldown.put(e.getUniqueId(), t);
                    Location dest = to.clone();
                    dest.setYaw(e.getLocation().getYaw());
                    dest.setPitch(e.getLocation().getPitch());
                    Vector vel = e.getVelocity();
                    Vector out = e.getLocation().getDirection().setY(0);
                    if (out.lengthSquared() < 0.01) out = new Vector(1, 0, 0);
                    dest.add(out.normalize().multiply(1.6));
                    e.teleport(dest);
                    e.setVelocity(vel);
                    s.sound(dest, "entity.enderman.teleport", 1.5f, 1.2f);
                    Fx.p(dest.getWorld(), Particle.REVERSE_PORTAL, dest.clone().add(0, 1, 0), 30, 0.4, 0.1);
                }
            }
        });
    }
}
