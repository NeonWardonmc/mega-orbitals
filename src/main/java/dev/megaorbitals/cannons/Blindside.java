package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.*;

/** Psychological warfare: darkness, blindness and terrifying fake sounds all around you. */
public final class Blindside extends Cannon {
    public Blindside() {
        super("&0&lBlindside", Material.INK_SAC, "Darkness, blindness and terrifying fake", "sounds all around. Then the real attack.");
        warning(10, "#101010");
        option("radius", 30, "Range.");
        option("seconds", 20, "How long it lasts.");
        option("final-strike", true, "End with a real explosion on each player.");
    }

    private static final String[] SOUNDS = {"entity.creeper.primed", "entity.tnt.primed", "entity.warden.heartbeat", "entity.zombie.ambient",
            "entity.skeleton.ambient", "entity.enderman.stare", "entity.ghast.warn", "entity.generic.explode", "entity.warden.roar",
            "entity.spider.ambient", "ambient.cave", "entity.wither.ambient"};

    @Override
    public void strike(Strike s) {
        Random rnd = new Random();
        int ticks = i("seconds") * 20;
        for (Player p : s.players(s.at(), d("radius"))) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, ticks + 40, 0));
            p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, ticks, 0));
        }
        s.every(15, ticks / 15, t -> {
            for (Player p : s.players(s.at(), d("radius") + 20)) {
                Location l = p.getLocation().add(rnd.nextGaussian() * 4, 0, rnd.nextGaussian() * 4);
                p.playSound(l, SOUNDS[rnd.nextInt(SOUNDS.length)], 1f, 0.8f + rnd.nextFloat() * 0.4f);
            }
        });
        if (b("final-strike")) {
            s.later(ticks, () -> {
                for (Player p : s.players(s.at(), d("radius") + 20)) s.explode(p.getLocation(), 3f, false);
            });
        }
    }
}
