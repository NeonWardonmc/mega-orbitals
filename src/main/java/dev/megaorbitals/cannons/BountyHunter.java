package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.*;

/** Puts a glowing bounty on the player nearest the target. Whoever kills them collects the reward. */
public final class BountyHunter extends Cannon {

    private static final class Bounty {
        final Strike s;
        boolean over;
        Bounty(Strike s) { this.s = s; }
    }

    private final Map<UUID, Bounty> bounties = new HashMap<>();

    public BountyHunter() {
        super("&6&lBounty Hunter", Material.GOLDEN_SWORD, "Puts a glowing bounty on a player.", "Whoever kills them collects the reward!");
        warning(10, "#FFB000");
        noBlockDamage();
        option("range", 30.0, "How far from the target to find the wanted player.");
        option("seconds", 300, "How long the bounty lasts.");
        option("reward", List.of("DIAMOND:5", "EMERALD:10", "GOLDEN_APPLE:2"), "Reward for the killer, MATERIAL:amount.");
        option("survivor-reward", List.of("DIAMOND:2"), "Given to the wanted player if they survive (empty list = nothing).");
        option("announce-to-server", true, "Tell everyone online about the bounty.");
    }

    @Override
    public void strike(Strike s) {
        Player wanted = s.nearestPlayer(s.at(), d("range"));
        if (wanted == null) { s.tell("&cNo player near the target to put a bounty on."); return; }
        if (bounties.containsKey(wanted.getUniqueId())) { s.tell("&c" + wanted.getName() + " already has a bounty."); return; }
        Bounty b = new Bounty(s);
        bounties.put(wanted.getUniqueId(), b);
        int secs = i("seconds");
        String reward = describe(list("reward"));
        announce(s, "&6&lBOUNTY! &f" + wanted.getName() + " &7is wanted for &e" + reward + "&7. Hunt them down!");
        Fx.title(wanted, "&6&lYOU ARE WANTED", "&7Survive " + secs + "s - everyone can see you.", 60);
        s.sound(wanted.getLocation(), "event.raid.horn", 4f, 1.4f);
        s.every(20, secs + 1, t -> {
            if (b.over) return;
            if (!wanted.isOnline()) {
                b.over = true;
                bounties.remove(wanted.getUniqueId());
                announce(s, "&7The bounty on &f" + wanted.getName() + " &7was cancelled - they fled the server.");
                return;
            }
            if (t >= secs) {
                b.over = true;
                bounties.remove(wanted.getUniqueId());
                wanted.removePotionEffect(PotionEffectType.GLOWING);
                announce(s, "&f" + wanted.getName() + " &7survived the bounty!");
                Fx.title(wanted, "&a&lYOU SURVIVED", "", 40);
                s.give(wanted, list("survivor-reward"));
                return;
            }
            wanted.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 40, 0, false, false));
            Fx.dustLine(wanted.getWorld(), wanted.getLocation().add(0, 2.2, 0), wanted.getLocation().add(0, 14, 0), Color.fromRGB(0xFFB000), 1.4f, 0.8);
            s.actionBar(wanted, "&6&lWANTED &7- &f" + (secs - t) + "s");
        });
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player victim = e.getEntity();
        Bounty b = bounties.remove(victim.getUniqueId());
        if (b == null || b.over) return;
        b.over = true;
        Player killer = victim.getKiller();
        Strike s = b.s;
        if (killer == null || killer.equals(victim)) {
            announce(s, "&f" + victim.getName() + " &7died, but nobody claimed the bounty.");
            return;
        }
        s.give(killer, list("reward"));
        announce(s, "&6&lBOUNTY CLAIMED! &f" + killer.getName() + " &7took down &f" + victim.getName() + "&7.");
        Fx.title(killer, "&6&lBOUNTY CLAIMED", "&7+" + describe(list("reward")), 50);
        killer.playSound(killer.getLocation(), "ui.toast.challenge_complete", 1f, 1f);
    }

    private void announce(Strike s, String msg) {
        Collection<? extends Player> to = b("announce-to-server") ? Bukkit.getOnlinePlayers() : s.world.getPlayers();
        for (Player p : to) p.sendMessage(MegaOrbitals.color(msg));
    }

    private static String describe(List<String> entries) {
        StringBuilder sb = new StringBuilder();
        for (var it : Strike.items(entries)) {
            if (!sb.isEmpty()) sb.append(", ");
            sb.append(it.getAmount()).append(" ").append(it.getType().name().toLowerCase(Locale.ROOT).replace('_', ' '));
        }
        return sb.isEmpty() ? "glory" : sb.toString();
    }
}
