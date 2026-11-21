package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.*;

/** A contagious plague: infected players and mobs spread it to anyone who gets close. */
public final class Plague extends Cannon {
    public Plague() {
        super("&2&lPlague", Material.FERMENTED_SPIDER_EYE, "A contagious sickness. The infected", "spread it to anyone who gets close.");
        warning(20, "#40A020");
        option("radius", 15, "Initial infection radius.");
        option("spread-distance", 3.5, "How close you must be to catch it.");
        option("sick-seconds", 60, "How long each infection lasts.");
        option("outbreak-seconds", 180, "How long the plague keeps spreading.");
        option("damage", 1.0, "Damage every second while sick.");
    }

    @Override
    public void strike(Strike s) {
        Map<UUID, Integer> sick = new HashMap<>();
        Color green = Color.fromRGB(60, 160, 40);
        for (LivingEntity le : s.victims(s.at(), d("radius"))) infect(s, le, sick, 0);
        Fx.dustSphere(s.world, s.at(0, 1, 0), d("radius"), green, 2f, 300);
        s.every(20, i("outbreak-seconds"), t -> {
            for (UUID id : new ArrayList<>(sick.keySet())) {
                Entity e = Bukkit.getEntity(id);
                if (!(e instanceof LivingEntity le) || le.isDead() || t - sick.get(id) > i("sick-seconds")) {
                    sick.remove(id);
                    if (e instanceof Player p) s.actionBar(p, "&aYou recovered from the plague.");
                    continue;
                }
                le.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 40, 0, false, true));
                le.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 40, 2, false, true));
                if (t % 5 == 0) le.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 100, 0, false, false));
                s.damage(le, d("damage"));
                Fx.dust(s.world, le.getLocation().add(0, 1, 0), green, 1.5f, 8, 0.4);
                for (LivingEntity near : s.victims(le.getLocation(), d("spread-distance"))) infect(s, near, sick, t);
            }
        });
    }

    private void infect(Strike s, LivingEntity le, Map<UUID, Integer> sick, int now) {
        if (sick.putIfAbsent(le.getUniqueId(), now) == null && le instanceof Player p) {
            Fx.title(p, "&2&lINFECTED", "&7Stay away from others... or don't.", 40);
            p.playSound(p.getLocation(), "entity.zombie_villager.converted", 1f, 0.6f);
        }
    }
}
