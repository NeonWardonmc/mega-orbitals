package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import java.util.*;

/** Minigame: GREEN LIGHT you can move. RED LIGHT, anyone who moves gets struck by lightning. */
public final class RedLightGreenLight extends Cannon {
    public RedLightGreenLight() {
        super("&c&lRed Light, &a&lGreen Light", Material.REDSTONE_LAMP, "Minigame: GREEN LIGHT you can move. RED LIGHT,", "anyone who moves gets struck by lightning.");
        warning(10, "#FF4040");
        noBlockDamage();
        option("radius", 40, "Range.");
        option("seconds", 45, "Length of the game.");
        option("damage", 8.0, "Damage for moving on red.");
    }

    @Override
    public void strike(Strike s) {
        Random rnd = new Random();
        boolean[] red = {false};
        int[] switchAt = {40};
        int[] redStart = {0};
        Map<UUID, Location> frozenAt = new HashMap<>();
        s.every(2, i("seconds") * 10, t -> {
            int tick = t * 2;
            if (tick >= switchAt[0]) {
                red[0] = !red[0];
                switchAt[0] = tick + (red[0] ? 40 + rnd.nextInt(30) : 50 + rnd.nextInt(50));
                redStart[0] = tick;
                frozenAt.clear();
                for (Player p : s.players(s.at(), d("radius"))) {
                    Fx.title(p, red[0] ? "&c&lRED LIGHT" : "&a&lGREEN LIGHT", red[0] ? "&7Don't move." : "&7Go!", 25);
                    p.playSound(p.getLocation(), red[0] ? "block.note_block.bass" : "block.note_block.chime", 1f, red[0] ? 0.5f : 2f);
                    if (red[0]) frozenAt.put(p.getUniqueId(), p.getLocation());
                }
                return;
            }
            if (!red[0]) return;
            boolean grace = tick - redStart[0] < 10;
            for (Player p : s.players(s.at(), d("radius"))) {
                Location was = frozenAt.get(p.getUniqueId());
                if (grace || was == null || was.getWorld() != p.getWorld()) { frozenAt.put(p.getUniqueId(), p.getLocation()); continue; }
                if (was.distanceSquared(p.getLocation()) > 0.04) {
                    s.world.strikeLightningEffect(p.getLocation());
                    s.damage(p, d("damage"));
                    frozenAt.put(p.getUniqueId(), p.getLocation());
                }
            }
        });
    }
}
