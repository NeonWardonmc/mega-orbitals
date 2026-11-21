package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import java.util.*;

/** Minigame: a glowing hill appears. Whoever stands on it longest wins the prize. */
public final class KingOfTheHill extends Cannon {
    public KingOfTheHill() {
        super("&6&lKing of the Hill", Material.GOLDEN_HELMET, "Minigame: a glowing hill appears. Whoever", "stands on it longest wins the prize.");
        warning(10, "#FFC000");
        noBlockDamage();
        option("zone-radius", 4.0, "Size of the hill zone.");
        option("seconds", 60, "Length of the game.");
        option("announce-radius", 80, "Players this close see the scoreboard.");
        option("prize", List.of("GOLDEN_APPLE:5", "DIAMOND:8", "ENCHANTED_GOLDEN_APPLE:1"), "Prize items, MATERIAL:amount.");
    }

    @Override
    public void strike(Strike s) {
        Map<UUID, Integer> score = new HashMap<>();
        BossBar bar = BossBar.bossBar(s.text("&6&lKING OF THE HILL"), 1f, BossBar.Color.YELLOW, BossBar.Overlay.PROGRESS);
        int secs = i("seconds");
        s.every(20, secs + 1, t -> {
            Fx.dustRing(s.world, s.at(0, 0.2, 0), d("zone-radius"), Color.YELLOW, 2f);
            Fx.dustLine(s.world, s.at(0, 30, 0), s.at(), Color.YELLOW, 2f, 1);
            for (Player p : s.world.getPlayers()) {
                double dist = p.getLocation().distance(s.at());
                if (dist <= i("announce-radius")) p.showBossBar(bar);
                if (dist <= d("zone-radius")) score.merge(p.getUniqueId(), 1, Integer::sum);
            }
            UUID lead = score.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse(null);
            String leader = lead == null ? "nobody" : Bukkit.getOfflinePlayer(lead).getName() + " (" + score.get(lead) + "s)";
            bar.name(s.text("&6&lKING OF THE HILL &8» &f" + leader + " &8| &e" + (secs - t) + "s"));
            bar.progress(Math.max(0, (secs - t) / (float) secs));
            if (t < secs) return;
            for (Player p : s.world.getPlayers()) p.hideBossBar(bar);
            Player winner = lead == null ? null : Bukkit.getPlayer(lead);
            if (winner == null) return;
            for (String entry : list("prize")) {
                String[] parts = entry.split(":");
                Material m = Material.matchMaterial(parts[0]);
                if (m == null) continue;
                int amt = parts.length > 1 ? Integer.parseInt(parts[1].trim()) : 1;
                winner.getInventory().addItem(new ItemStack(m, amt)).values().forEach(l -> winner.getWorld().dropItem(winner.getLocation(), l));
            }
            for (Player p : s.world.getPlayers()) if (p.getLocation().distance(s.at()) <= i("announce-radius"))
                Fx.title(p, "&6&l👑 " + winner.getName(), "&eis King of the Hill!", 60);
            s.sound(s.at(), "ui.toast.challenge_complete", 10f, 1f);
        });
    }
}
