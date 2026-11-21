package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.*;

/** Sets up an invisible alarm zone. Anyone who walks in is revealed and you get pinged. */
public final class Tripwire extends Cannon {
    public Tripwire() {
        super("&7&lOrbital Tripwire", Material.TRIPWIRE_HOOK, "An invisible alarm zone. Anyone who walks in", "glows and you get pinged with their name.");
        warning(5, "#A0A0A0");
        noBlockDamage();
        option("radius", 25, "Size of the alarm zone.");
        option("minutes", 10, "How long the alarm stays armed.");
        option("strike-intruders", false, "Also hit intruders with lightning.");
    }

    @Override
    public void strike(Strike s) {
        Set<UUID> seen = new HashSet<>();
        if (s.shooter != null) s.actionBar(s.shooter, "&7Tripwire armed for " + i("minutes") + " minutes.");
        s.every(10, i("minutes") * 120, t -> {
            for (Player p : s.players(s.at(), d("radius"))) {
                if (!seen.add(p.getUniqueId())) continue;
                p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 600, 0));
                if (b("strike-intruders")) s.world.strikeLightningEffect(p.getLocation());
                if (s.shooter != null && s.shooter.isOnline()) {
                    s.shooter.sendMessage(s.text("&c&l[TRIPWIRE] &f" + p.getName() + " &7entered your zone at &f"
                            + p.getLocation().getBlockX() + ", " + p.getLocation().getBlockY() + ", " + p.getLocation().getBlockZ()));
                    s.shooter.playSound(s.shooter.getLocation(), "block.note_block.bell", 1f, 2f);
                }
            }
            for (UUID id : new ArrayList<>(seen)) {
                Player p = Bukkit.getPlayer(id);
                if (p == null || p.getWorld() != s.world || p.getLocation().distance(s.at()) > d("radius") + 5) seen.remove(id);
            }
        });
    }
}
