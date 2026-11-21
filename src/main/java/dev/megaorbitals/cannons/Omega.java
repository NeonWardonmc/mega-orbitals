package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import java.util.*;

/**
 * Cannon #201. Fires EVERY other cannon at the same target at once.
 * The launches are spread over a few ticks (stagger-ticks) so the server doesn't freeze in one go.
 */
public final class Omega extends Cannon {
    public Omega() {
        super("&4&l&k||&r &4&lΩ OMEGA Ω &4&l&k||", Material.COMMAND_BLOCK,
                "Fires every other cannon at once.", "All of them. At the same spot.", "&cThe server may not survive.");
        warning(300, "#FF0000");
        option("exclude", List.of(), "Cannon ids Omega should skip, e.g. [meganuke, exile].");
        option("max-cannons", 0, "Fire at most this many cannons, picked at random (0 = every cannon).");
        option("stagger-ticks", 1, "Ticks between each cannon launch (0 = truly all at once).");
        option("skip-disabled", true, "Skip cannons disabled in config.yml.");
        option("include-warnings", false, "Also play every cannon's own warning (very noisy).");
    }

    @Override
    public void warn(Strike s) {
        super.warn(s);
        for (Player p : s.world.getPlayers()) Fx.title(p, "&4&lΩ OMEGA PROTOCOL Ω", "&cEvery orbital cannon is locking on.", 80);
        int secs = warningTicks() / 20;
        s.every(20, secs, t -> {
            int left = secs - t;
            for (Player p : s.world.getPlayers()) s.actionBar(p, "&4&lΩ ALL CANNONS FIRE IN " + left + "s Ω");
            s.soundWorld("block.note_block.basedrum", 1f, 0.5f);
            if (left <= 10) s.soundWorld("block.note_block.pling", 1f, 2f);
            Fx.dustRing(s.world, s.at(0, 0.3, 0), 3 + t % 20, Color.RED, 3f);
        });
    }

    @Override
    public void strike(Strike s) {
        Set<String> skip = new HashSet<>();
        for (String x : list("exclude")) skip.add(x.toLowerCase());
        List<Cannon> all = new ArrayList<>();
        for (Cannon c : s.plugin.cannons().values()) {
            if (c == this || skip.contains(c.id())) continue;
            if (b("skip-disabled") && s.plugin.settings().disabled(c)) continue;
            all.add(c);
        }
        if (i("max-cannons") > 0 && all.size() > i("max-cannons")) {
            Collections.shuffle(all);
            all = new ArrayList<>(all.subList(0, i("max-cannons")));
        }
        int stagger = Math.max(0, i("stagger-ticks"));
        boolean warnings = b("include-warnings");
        s.soundWorld("entity.wither.spawn", 1f, 0.5f);
        s.soundWorld("entity.ender_dragon.death", 1f, 0.5f);
        for (Player p : s.world.getPlayers()) Fx.title(p, "&4&lΩ", "&c" + all.size() + " cannons inbound", 40);
        s.plugin.getLogger().info("OMEGA fired by " + (s.shooter == null ? "?" : s.shooter.getName()) + ": launching " + all.size() + " cannons.");
        for (int k = 0; k < all.size(); k++) {
            Cannon c = all.get(k);
            s.later((long) k * stagger, () -> {
                if (warnings) {
                    s.plugin.fireAt(c, s.block(), s.shooter);
                } else {
                    Strike sub = new Strike(s.plugin, c, s.block(), s.shooter);
                    try { c.strike(sub); } catch (Exception e) { s.plugin.getLogger().warning("Omega: " + c.id() + " failed: " + e); }
                }
            });
        }
    }
}
