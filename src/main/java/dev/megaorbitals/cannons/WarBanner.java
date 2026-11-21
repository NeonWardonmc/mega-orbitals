package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.*;

/** Plants a war banner. You and the players who were with you fight stronger while near it. */
public final class WarBanner extends Cannon {
    public WarBanner() {
        super("&c&lWar Banner", Material.RED_BANNER, "Plants a war banner. You and your squad", "fight stronger while standing near it.");
        warning(5, "#C02020");
        noBlockDamage();
        option("squad-radius", 10, "Players this close to you when you fire count as your squad.");
        option("aura-radius", 14, "How close to the banner you must stay.");
        option("seconds", 120, "How long the banner stands.");
    }

    @Override
    public void strike(Strike s) {
        Set<UUID> squad = new HashSet<>();
        for (Player p : s.allies(d("squad-radius"))) squad.add(p.getUniqueId());
        ArmorStand banner = s.world.spawn(s.at(), ArmorStand.class, a -> {
            a.setInvisible(true);
            a.setMarker(true);
            a.getEquipment().setHelmet(new org.bukkit.inventory.ItemStack(Material.RED_BANNER));
        });
        s.tag(banner);
        s.every(40, i("seconds") / 2, t -> {
            Fx.dustRing(s.world, s.at(0, 0.2, 0), d("aura-radius"), Color.RED, 1.2f);
            for (Player p : s.world.getPlayers()) {
                if (!squad.contains(p.getUniqueId()) || p.getLocation().distance(s.at()) > d("aura-radius")) continue;
                p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 50, 1, true, true));
                p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 50, 0, true, true));
                p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 50, 0, true, true));
            }
            if (t == i("seconds") / 2 - 1) banner.remove();
        });
        s.sound(s.at(), "event.raid.horn", 6f, 1.2f);
    }
}
