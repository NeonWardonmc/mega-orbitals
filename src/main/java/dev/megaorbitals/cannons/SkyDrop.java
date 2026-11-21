package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;

/** Teleports everyone nearby high into the sky. Hope you brought a water bucket. */
public final class SkyDrop extends Cannon {
    public SkyDrop() {
        super("&b&lSky Drop", Material.FEATHER, "Teleports everyone nearby high into the sky.", "Hope you packed a water bucket.");
        warning(20, "#80C0FF");
        noBlockDamage();
        option("radius", 25, "Range.");
        option("height", 200, "How far up they go.");
        option("disable-elytra", true, "Stop elytra from saving them.");
    }

    @Override
    public void strike(Strike s) {
        for (LivingEntity le : s.victims(s.at(), d("radius"))) {
            Location to = le.getLocation().add(0, i("height"), 0);
            to.setY(Math.min(to.getY(), s.world.getMaxHeight() + 100));
            Fx.p(s.world, Particle.CLOUD, le.getLocation(), 40, 0.5, 0.2);
            le.teleport(to);
            if (le instanceof Player p) {
                if (b("disable-elytra")) p.setCooldown(Material.FIREWORK_ROCKET, 400);
                Fx.title(p, "&b&lLOOK DOWN", "", 40);
            }
        }
        s.sound(s.at(), "entity.breeze.wind_burst", 8f, 0.5f);
        if (b("disable-elytra")) {
            s.every(1, 200, t -> {
                for (Player p : s.players(s.at(), d("radius") + 50)) p.setGliding(false);
            });
        }
    }
}
