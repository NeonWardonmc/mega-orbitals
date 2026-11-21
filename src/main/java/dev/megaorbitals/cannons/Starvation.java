package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.*;

/** Famine: everyone's hunger is drained to zero and they can't eat or drink anything for a while. */
public final class Starvation extends Cannon {
    private final Map<UUID, Long> starving = new HashMap<>();

    public Starvation() {
        super("&6&lFamine", Material.BOWL, "Everyone's hunger drains to zero and they", "can't eat or drink anything for a while.");
        warning(10, "#A08040");
        noBlockDamage();
        option("radius", 25, "Range.");
        option("seconds", 45, "How long the famine lasts.");
    }

    @Override
    public void strike(Strike s) {
        long until = System.currentTimeMillis() + i("seconds") * 1000L;
        for (Player p : s.players(s.at(), d("radius"))) {
            starving.put(p.getUniqueId(), until);
            p.setFoodLevel(0);
            p.setSaturation(0);
            p.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, i("seconds") * 20, 2));
            Fx.title(p, "&6&lFAMINE", "&7You can't eat for " + i("seconds") + "s", 30);
        }
        s.sound(s.at(), "entity.player.burp", 8f, 0.4f);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEat(PlayerItemConsumeEvent e) {
        Long u = starving.get(e.getPlayer().getUniqueId());
        if (u == null) return;
        if (u < System.currentTimeMillis()) { starving.remove(e.getPlayer().getUniqueId()); return; }
        e.setCancelled(true);
        e.getPlayer().sendActionBar(MegaOrbitals.color("&6Famine! You can't eat."));
    }
}
