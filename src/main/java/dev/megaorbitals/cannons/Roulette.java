package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import java.util.*;

/** Spins the wheel and fires a random cannon at the target. Could be anything. */
public final class Roulette extends Cannon {
    public Roulette() {
        super("&6&lOrbital Roulette", Material.COMPASS, "Spins the wheel and fires a random cannon", "at the target. Could be anything.");
        warning(40, "#FFC000");
        option("exclude", List.of("meganuke", "omega", "roulette"), "Cannons the wheel can never land on.");
        option("spins", 1, "How many random cannons it fires.");
    }

    @Override
    public void warn(Strike s) {
        super.warn(s);
        List<String> ids = new ArrayList<>(s.plugin.cannons().keySet());
        s.every(4, warningTicks() / 4, t -> {
            if (s.shooter != null) s.actionBar(s.shooter, "&6🎲 " + ids.get(new Random().nextInt(ids.size())));
            s.sound(s.at(), "block.note_block.hat", 4f, 1f + t * 0.05f);
        });
    }

    @Override
    public void strike(Strike s) {
        List<String> exclude = new ArrayList<>();
        for (String x : list("exclude")) exclude.add(x.toLowerCase());
        List<Cannon> pool = new ArrayList<>();
        for (Cannon c : s.plugin.cannons().values()) {
            if (c != this && !exclude.contains(c.id()) && !s.plugin.settings().disabled(c)) pool.add(c);
        }
        if (pool.isEmpty()) return;
        Random rnd = new Random();
        List<String> names = new ArrayList<>();
        for (int k = 0; k < Math.max(1, i("spins")); k++) {
            Cannon pick = pool.get(rnd.nextInt(pool.size()));
            names.add(pick.name());
            s.plugin.fireAt(pick, s.block(), s.shooter, s);
        }
        if (s.shooter != null) s.actionBar(s.shooter, "&6🎲 The wheel landed on: &r" + String.join("&7, &r", names));
        s.sound(s.at(), "entity.player.levelup", 6f, 1f);
    }
}
