package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import java.util.*;

/** Records a player's every step for a few seconds, then a damaging echo retraces their exact path. */
public final class EchoStrike extends Cannon {
    public EchoStrike() {
        super("&3&lEcho Strike", Material.ECHO_SHARD, "Records a player's movement, then a damaging", "echo retraces their exact path.");
        warning(10, "#2080A0");
        noBlockDamage();
        option("range", 30.0, "How far from the target to find the player.");
        option("record-seconds", 10, "How long their movement is recorded.");
        option("echo-speed", 1, "How many recorded steps the echo moves each tick (1 = same speed, 2 = double...).");
        option("hit-radius", 2.0, "How close the echo must pass to hurt.");
        option("damage", 6.0, "Damage of each echo hit.");
    }

    @Override
    public void strike(Strike s) {
        Player p = s.nearestPlayer(s.at(), d("range"));
        if (p == null) { s.tell("&cNo player near the target to echo."); return; }
        List<Location> path = new ArrayList<>();
        int rec = i("record-seconds") * 20;
        Fx.title(p, "&3&lYOU ARE BEING RECORDED", "&7Every step you take will come back...", 40);
        s.sound(p.getLocation(), "block.sculk_sensor.clicking", 2f, 0.8f);
        s.every(1, rec, t -> {
            if (!p.isOnline() || p.getWorld() != s.world) return;
            path.add(p.getLocation());
            if (t % 3 == 0) Fx.dust(s.world, p.getLocation().add(0, 0.1, 0), Color.fromRGB(0x2080A0), 0.8f, 1, 0.05);
            if (t % 20 == 0) s.actionBar(p, "&3Recording... &f" + (rec - t) / 20 + "s");
        });
        s.later(rec + 10, () -> {
            if (path.isEmpty()) return;
            s.sound(path.get(0), "entity.warden.sonic_charge", 3f, 1.4f);
            Map<UUID, Integer> lastHit = new HashMap<>();
            int speed = Math.max(1, i("echo-speed"));
            int[] idx = {0};
            s.loop(1, t -> {
                if (idx[0] >= path.size()) {
                    Location end = path.get(path.size() - 1);
                    Fx.p(s.world, Particle.SONIC_BOOM, end.clone().add(0, 1, 0), 1, 0, 0);
                    s.sound(end, "entity.warden.sonic_boom", 3f, 1.2f);
                    return false;
                }
                Location l = path.get(idx[0]);
                idx[0] += speed;
                Location mid = l.clone().add(0, 1, 0);
                Fx.p(s.world, Particle.SCULK_SOUL, mid, 3, 0.25, 0.02);
                Fx.dust(s.world, mid, Color.fromRGB(0x30D0FF), 1.6f, 6, 0.3);
                if (t % 4 == 0) s.sound(l, "block.sculk.spread", 1.5f, 1.4f);
                for (LivingEntity le : s.world.getNearbyLivingEntities(mid, d("hit-radius"))) {
                    if (le.isDead() || s.spared(le)) continue;
                    Integer last = lastHit.get(le.getUniqueId());
                    if (last != null && t - last < 10) continue;
                    lastHit.put(le.getUniqueId(), t);
                    s.damage(le, d("damage"));
                    Fx.p(s.world, Particle.SONIC_BOOM, le.getLocation().add(0, 1, 0), 1, 0, 0);
                }
                return true;
            });
        });
    }
}
