package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;
import java.util.concurrent.ThreadLocalRandom;

/** A colossal warden or ravager crashes down and stomps through the area, flattening everything for a minute. */
public final class Kaiju extends Cannon {
    public Kaiju() {
        super("&4&lKaiju", Material.SCULK_SHRIEKER, "A colossal warden or ravager stomps", "through the area for a minute.");
        warning(60, "#600020");
        option("monster", "RANDOM", "WARDEN, RAVAGER or RANDOM.");
        option("scale", 6.0, "How big it is (1 = normal size, max 16).");
        option("seconds", 60, "How long it rampages before vanishing.");
        option("health", 600.0, "Its health.");
        option("stomp-damage", 10.0, "Damage of each footstep shockwave.");
        option("stomp-radius", 7.0, "Size of each footstep shockwave.");
        option("stomp-power", 2.0, "Explosion power of each footstep (0 = no block damage from steps).");
        option("roam-radius", 40.0, "It hunts players this close; otherwise it wanders around the target.");
    }

    @Override
    public void strike(Strike s) {
        String pick = str("monster").toUpperCase();
        boolean warden = pick.equals("WARDEN") || (!pick.equals("RAVAGER") && ThreadLocalRandom.current().nextBoolean());
        double scale = Math.max(1, Math.min(16, d("scale")));
        Class<? extends Mob> type = warden ? Warden.class : Ravager.class;
        Mob k = s.mob(s.at(0, 30, 0), type, m -> {
            m.setPersistent(true);
            m.setRemoveWhenFarAway(false);
            m.customName(s.text("&4&lKAIJU"));
            set(m, Attribute.SCALE, scale);
            set(m, Attribute.MAX_HEALTH, d("health"));
            m.setHealth(d("health"));
            set(m, Attribute.STEP_HEIGHT, Math.max(1, scale * 0.6));
            set(m, Attribute.SAFE_FALL_DISTANCE, 1000);
            set(m, Attribute.KNOCKBACK_RESISTANCE, 1);
            set(m, Attribute.FOLLOW_RANGE, d("roam-radius") + 10);
            var spd = m.getAttribute(Attribute.MOVEMENT_SPEED);
            if (spd != null) spd.setBaseValue(spd.getBaseValue() * 1.1);
        });
        s.soundWorld("entity.ender_dragon.growl", 0.8f, 0.4f);
        for (Player p : s.world.getPlayers()) if (p.getLocation().distance(s.at()) < 120) Fx.title(p, "&4&lKAIJU", "&7Run.", 50);
        int life = i("seconds") * 20;
        Location[] last = {null};
        boolean[] landed = {false};
        s.loop(1, t -> {
            if (!k.isValid() || k.isDead()) return false;
            if (t >= life) {
                Fx.p(s.world, Particle.EXPLOSION_EMITTER, k.getLocation().add(0, scale, 0), 4, scale / 2, 0);
                Fx.p(s.world, Particle.LARGE_SMOKE, k.getLocation().add(0, scale, 0), 200, scale / 2, 0.05);
                s.sound(k.getLocation(), warden ? "entity.warden.dig" : "entity.ravager.death", 6f, 0.5f);
                k.remove();
                return false;
            }
            if (!landed[0]) {
                if (!k.isOnGround() || t < 3) return true;
                landed[0] = true;
                stomp(s, k, k.getLocation(), d("stomp-radius") * 1.5, d("stomp-damage") * 1.5, (float) d("stomp-power") * 1.5f);
                last[0] = k.getLocation();
                return true;
            }
            if (t % 20 == 0) {
                Player target = s.nearestPlayer(k.getLocation(), d("roam-radius"));
                if (target != null) {
                    k.setTarget(target);
                    if (k instanceof Warden w) w.setAnger(target, 150);
                } else if (k.getTarget() == null) {
                    k.getPathfinder().moveTo(s.randomPoint(d("roam-radius") * 0.5));
                }
                if (t % 100 == 0) s.sound(k.getLocation(), warden ? "entity.warden.roar" : "entity.ravager.roar", 8f, 0.5f);
            }
            Location now = k.getLocation();
            double stride = Math.max(2, scale * 0.8);
            if (last[0] != null && now.getWorld() == last[0].getWorld() && now.distance(last[0]) >= stride && k.isOnGround()) {
                last[0] = now;
                stomp(s, k, now, d("stomp-radius"), d("stomp-damage"), (float) d("stomp-power"));
            }
            return true;
        });
    }

    private void stomp(Strike s, Mob k, Location l, double r, double dmg, float power) {
        Fx.p(s.world, Particle.EXPLOSION, l, 4, r / 3, 0);
        Fx.block(s.world, Particle.BLOCK, l, l.clone().subtract(0, 1, 0).getBlock().getType().isAir() ? Material.DIRT
                : l.clone().subtract(0, 1, 0).getBlock().getType(), 120, r / 2);
        s.sound(l, "entity.generic.explode", 3f, 0.4f);
        s.sound(l, "entity.warden.step", 6f, 0.4f);
        for (LivingEntity le : s.victims(l, r)) {
            if (le.equals(k)) continue;
            s.damage(le, dmg);
            le.setVelocity(Fx.dir(l, le.getLocation()).multiply(1.1).add(new Vector(0, 0.7, 0)));
        }
        if (power > 0) s.explode(l.clone().subtract(0, 0.5, 0), power, false);
    }

    private static void set(LivingEntity e, Attribute a, double v) {
        var inst = e.getAttribute(a);
        if (inst != null) inst.setBaseValue(v);
    }
}
