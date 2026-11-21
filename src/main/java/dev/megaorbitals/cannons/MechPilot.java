package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import io.papermc.paper.entity.TeleportFlag;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import java.util.*;

/** Drops a giant iron golem mech. The shooter climbs in: it walks where they steer and punches when they swing. */
public final class MechPilot extends Cannon {

    private static final class Mech {
        final Strike s;
        final IronGolem golem;
        int lastPunch = -100, tick;
        Mech(Strike s, IronGolem golem) { this.s = s; this.golem = golem; }
    }

    private final Map<UUID, Mech> pilots = new HashMap<>();

    public MechPilot() {
        super("&7&lMech Pilot", Material.IRON_BLOCK, "Drops a giant iron golem mech for you to ride.", "Steer with WASD, swing to punch. Sneak to get out.");
        warning(25, "#C0C0C0");
        noBlockDamage();
        option("seconds", 90, "How long the mech lasts.");
        option("scale", 3.0, "Size of the mech (1 = normal golem).");
        option("speed", 0.35, "Walking speed (blocks per tick).");
        option("punch-damage", 16.0, "Damage of each punch.");
        option("punch-range", 5.0, "How far the punch reaches.");
        option("punch-cooldown-ticks", 15, "Ticks between punches.");
        option("health", 300.0, "Mech health.");
    }

    @Override
    public void strike(Strike s) {
        Player pilot = s.shooter;
        if (pilot == null || !pilot.isOnline()) return;
        if (pilots.containsKey(pilot.getUniqueId())) { s.tell("&cYou are already piloting a mech."); return; }
        double scale = Math.max(1, Math.min(10, d("scale")));
        IronGolem golem = s.mob(s.at(), IronGolem.class, g -> {
            g.setAI(false);
            g.setPersistent(true);
            g.setPlayerCreated(true);
            g.customName(s.text("&7&l" + pilot.getName() + "'s Mech"));
            var sc = g.getAttribute(Attribute.SCALE);
            if (sc != null) sc.setBaseValue(scale);
            var hp = g.getAttribute(Attribute.MAX_HEALTH);
            if (hp != null) { hp.setBaseValue(d("health")); g.setHealth(d("health")); }
        });
        Fx.p(s.world, Particle.EXPLOSION_EMITTER, s.at(), 1, 0, 0);
        s.sound(s.at(), "entity.iron_golem.repair", 4f, 0.5f);
        Mech m = new Mech(s, golem);
        pilots.put(pilot.getUniqueId(), m);
        golem.addPassenger(pilot);
        Fx.title(pilot, "&7&lMECH ONLINE", "&fWASD &7to walk, &fswing &7to punch, &fsneak &7to exit", 60);
        int life = i("seconds") * 20;
        int[] away = {0};
        s.loop(1, t -> {
            m.tick = t;
            boolean riding = pilot.isOnline() && golem.getPassengers().contains(pilot);
            away[0] = riding ? 0 : away[0] + 1;
            if (!golem.isValid() || t >= life || away[0] > 40) {
                end(pilot, m);
                return false;
            }
            if (!riding) return true;
            if (t % 20 == 0) s.actionBar(pilot, "&7Mech: &f" + (life - t) / 20 + "s &8| &c" + (int) golem.getHealth() + " HP");
            Input in = pilot.getCurrentInput();
            Vector fwd = pilot.getLocation().getDirection().setY(0);
            if (fwd.lengthSquared() < 0.01) fwd = golem.getLocation().getDirection().setY(0);
            fwd.normalize();
            Vector right = new Vector(-fwd.getZ(), 0, fwd.getX());
            Vector move = new Vector();
            if (in.isForward()) move.add(fwd);
            if (in.isBackward()) move.subtract(fwd.clone().multiply(0.6));
            if (in.isRight()) move.add(right.clone().multiply(0.7));
            if (in.isLeft()) move.subtract(right.clone().multiply(0.7));
            Location cur = golem.getLocation();
            Location next = cur.clone();
            if (move.lengthSquared() > 0.01) next.add(move.normalize().multiply(d("speed") * (in.isSprint() ? 1.5 : 1)));
            Double y = ground(next, (int) Math.ceil(scale));
            if (y == null) { next = cur.clone(); y = ground(next, (int) Math.ceil(scale)); }
            if (y != null) {
                double ny = Math.abs(y - cur.getY()) < 0.05 ? y : y < cur.getY() ? Math.max(y, cur.getY() - 0.6) : Math.min(y, cur.getY() + 0.5);
                next.setY(ny);
            } else next.setY(cur.getY() - 0.6);
            next.setYaw(pilot.getLocation().getYaw());
            next.setPitch(0);
            golem.teleport(next, TeleportFlag.EntityState.RETAIN_PASSENGERS);
            if (move.lengthSquared() > 0.01 && t % 10 == 0) {
                s.sound(next, "entity.iron_golem.step", 2f, 0.5f);
                Fx.block(s.world, Particle.BLOCK, next, next.clone().subtract(0, 1, 0).getBlock().getType().isAir() ? Material.DIRT
                        : next.clone().subtract(0, 1, 0).getBlock().getType(), 20, 1);
            }
            return true;
        });
    }

    /** Standing height for the mech at l: the top of the ground within step reach, or null if a wall blocks it. */
    private static Double ground(Location l, int step) {
        World w = l.getWorld();
        int x = l.getBlockX(), z = l.getBlockZ(), y0 = l.getBlockY();
        if (!Blocks.loaded(w, x, z)) return null;
        for (int y = y0 + step - 1; y >= y0 - 12; y--) {
            Block b = w.getBlockAt(x, y, z);
            if (b.getType().isSolid() && !b.getRelative(0, 1, 0).getType().isSolid() && !b.getRelative(0, 2, 0).getType().isSolid()) return (double) y + 1;
        }
        return null;
    }

    private void end(Player pilot, Mech m) {
        pilots.remove(pilot.getUniqueId());
        if (m.golem.isValid()) {
            m.golem.eject();
            Fx.p(m.s.world, Particle.EXPLOSION, m.golem.getLocation().add(0, 2, 0), 4, 1, 0);
            m.s.sound(m.golem.getLocation(), "entity.iron_golem.death", 3f, 0.6f);
            m.golem.remove();
        }
        if (pilot.isOnline()) {
            pilot.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 60, 0));
            pilot.sendActionBar(MegaOrbitals.color("&7Mech offline."));
        }
    }

    @EventHandler
    public void onSwing(PlayerAnimationEvent e) {
        Mech m = pilots.get(e.getPlayer().getUniqueId());
        if (m == null || !m.golem.isValid() || !m.golem.getPassengers().contains(e.getPlayer())) return;
        if (m.tick - m.lastPunch < i("punch-cooldown-ticks")) return;
        m.lastPunch = m.tick;
        Strike s = m.s;
        IronGolem g = m.golem;
        g.swingMainHand();
        g.playEffect(EntityEffect.ENTITY_ATTACK);
        Vector fwd = e.getPlayer().getLocation().getDirection();
        Location hit = g.getLocation().add(0, g.getHeight() * 0.5, 0).add(fwd.clone().multiply(d("punch-range") * 0.6));
        Fx.p(s.world, Particle.EXPLOSION, hit, 1, 0, 0);
        s.sound(hit, "entity.iron_golem.attack", 3f, 0.6f);
        for (LivingEntity le : s.victims(hit, d("punch-range") * 0.6)) {
            if (le.equals(g)) continue;
            s.damage(le, d("punch-damage"));
            le.setVelocity(fwd.clone().setY(0).normalize().multiply(1.6).setY(0.9));
        }
    }

    @EventHandler
    public void onHurtOwnMech(EntityDamageByEntityEvent e) {
        Mech m = pilots.get(e.getDamager().getUniqueId());
        if (m != null && e.getEntity().equals(m.golem)) e.setCancelled(true);
    }
}
