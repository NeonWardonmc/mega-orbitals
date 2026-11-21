package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;

/** Sticks a ticking bomb on the nearest player. Touch someone to pass it on before it blows! */
public final class HotPotato extends Cannon {
    public HotPotato() {
        super("&6&lHot Potato", Material.BAKED_POTATO, "Sticks a ticking bomb on the nearest player.", "Touch someone to pass it on before it blows!");
        warning(10, "#FFA040");
        option("range", 40, "How far to look for the first holder.");
        option("seconds", 20, "Fuse length.");
        option("pass-distance", 2.5, "How close you must get to pass it on.");
        option("power", 5.0, "Explosion power.");
    }

    @Override
    public void strike(Strike s) {
        Player first = s.nearestPlayer(s.at(), d("range"));
        if (first == null) {
            if (s.shooter != null) s.actionBar(s.shooter, "&cNo one to give the hot potato to.");
            return;
        }
        Player[] holder = {first};
        int[] lastPass = {-40};
        Fx.title(first, "&6&lHOT POTATO!", "&7Touch someone to pass it on!", 40);
        int secs = i("seconds");
        s.every(1, secs * 20 + 1, t -> {
            Player h = holder[0];
            if (!h.isOnline() || h.isDead()) return;
            int left = secs - t / 20;
            Fx.p(s.world, Particle.FLAME, h.getLocation().add(0, 2.3, 0), 2, 0.1, 0.01);
            if (t % 20 == 0) {
                s.actionBar(h, "&6&l💣 " + left + "s &7- pass it on!");
                h.playSound(h.getLocation(), "block.note_block.hat", 1f, left <= 5 ? 2f : 1f);
            }
            if (t - lastPass[0] > 20) {
                for (Player p : s.players(h.getLocation(), d("pass-distance"))) {
                    if (p.equals(h)) continue;
                    holder[0] = p;
                    lastPass[0] = t;
                    Fx.title(p, "&6&lHOT POTATO!", "&7Pass it on!", 30);
                    s.sound(p.getLocation(), "entity.item.pickup", 2f, 0.5f);
                    break;
                }
            }
            if (t == secs * 20) s.explode(holder[0].getLocation(), (float) d("power"), false);
        });
    }
}
