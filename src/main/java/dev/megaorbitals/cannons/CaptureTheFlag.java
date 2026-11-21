package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;
import java.util.*;

/** Minigame: nearby players split into Red and Blue. Grab the enemy flag and bring it home to score. */
public final class CaptureTheFlag extends Cannon {
    public CaptureTheFlag() {
        super("&9&lCapture &c&lthe Flag", Material.RED_BANNER, "Minigame: two bases, two flags. Nearby players", "split into teams - steal the flag and bring it home!");
        warning(20, "#8040FF");
        noBlockDamage();
        option("range", 40.0, "Players this close to the target join the game.");
        option("base-distance", 20.0, "Distance from the target to each base.");
        option("captures-to-win", 3, "Captures needed to win.");
        option("seconds", 300, "Time limit.");
        option("prize", List.of("DIAMOND:3", "GOLDEN_APPLE:2"), "Given to every player on the winning team.");
    }

    private static final class Team {
        final String name, colour;
        final Color dye;
        final Material banner;
        final Location base;
        final List<UUID> members = new ArrayList<>();
        ItemDisplay flag;
        UUID carrier;   // enemy player carrying this team's flag
        int score;
        Team(String name, String colour, Color dye, Material banner, Location base) {
            this.name = name; this.colour = colour; this.dye = dye; this.banner = banner; this.base = base;
        }
    }

    @Override
    public void strike(Strike s) {
        List<Player> ps = new ArrayList<>(s.world.getPlayers());
        ps.removeIf(p -> p.getLocation().distance(s.at()) > d("range") || p.getGameMode() == GameMode.SPECTATOR);
        if (ps.size() < 2) { s.tell("&cCapture the Flag needs at least 2 players near the target."); return; }
        Collections.shuffle(ps);
        Vector axis = s.shooter != null ? s.shooter.getLocation().getDirection().setY(0) : new Vector(1, 0, 0);
        if (axis.lengthSquared() < 0.01) axis = new Vector(1, 0, 0);
        axis.normalize();
        double dist = d("base-distance");
        Team red = new Team("Red", "&c", Color.RED, Material.RED_BANNER, s.surface(s.at().add(axis.clone().multiply(dist))));
        Team blue = new Team("Blue", "&9", Color.BLUE, Material.BLUE_BANNER, s.surface(s.at().add(axis.clone().multiply(-dist))));
        Team[] teams = {red, blue};
        for (int k = 0; k < ps.size(); k++) {
            Team t = teams[k % 2];
            Player p = ps.get(k);
            t.members.add(p.getUniqueId());
            Location spawn = t.base.clone().add(Math.random() * 3 - 1.5, 0, Math.random() * 3 - 1.5);
            spawn.setDirection(teams[(k + 1) % 2].base.toVector().subtract(t.base.toVector()));
            p.teleport(spawn);
            Fx.title(p, t.colour + "&lTEAM " + t.name.toUpperCase(), "&7Steal the other flag and bring it to your base!", 60);
        }
        for (Team t : teams) {
            t.flag = Fx.itemDisplay(t.base.clone().add(0, 1.6, 0), t.banner, 1.5f);
            t.flag.setBillboard(Display.Billboard.VERTICAL);
            s.tag(t.flag);
        }
        BossBar bar = BossBar.bossBar(s.text("CTF"), 1f, BossBar.Color.PURPLE, BossBar.Overlay.PROGRESS);
        Set<Player> shown = new HashSet<>();
        int secs = i("seconds"), win = Math.max(1, i("captures-to-win"));
        s.sound(s.at(), "event.raid.horn", 6f, 1.3f);
        s.loop(1, t -> {
            int left = secs - t / 20;
            for (Team team : teams) {
                Team enemy = team == red ? blue : red;
                Fx.dustRing(s.world, team.base.clone().add(0, 0.2, 0), 2.5, team.dye, 1.6f);
                if (t % 4 == 0) Fx.dustLine(s.world, team.base, team.base.clone().add(0, 12, 0), team.dye, 1.2f, 1);
                Player carrier = team.carrier == null ? null : Bukkit.getPlayer(team.carrier);
                if (team.carrier != null && (carrier == null || carrier.isDead() || carrier.getWorld() != s.world)) {
                    team.carrier = null;
                    if (carrier != null) carrier.setGlowing(false);
                    broadcast(teams, team.colour + team.name + " flag &7was dropped and returned to base.");
                    carrier = null;
                }
                if (carrier == null) {
                    team.flag.teleport(team.base.clone().add(0, 1.6 + Math.sin(t * 0.1) * 0.15, 0));
                    for (UUID id : enemy.members) {
                        Player p = Bukkit.getPlayer(id);
                        if (p == null || p.isDead() || p.getWorld() != s.world || p.getLocation().distance(team.base) > 1.8) continue;
                        team.carrier = id;
                        p.setGlowing(true);
                        broadcast(teams, enemy.colour + p.getName() + " &7took the " + team.colour + team.name + " flag&7!");
                        s.sound(team.base, "item.armor.equip_leather", 3f, 0.8f);
                        break;
                    }
                } else {
                    team.flag.teleport(carrier.getLocation().add(0, 2.6, 0));
                    if (t % 20 == 0) s.actionBar(carrier, "&eBring the flag back to your base!");
                    boolean homeFlagSafe = enemy.carrier == null;
                    if (carrier.getLocation().distance(enemy.base) <= 2.5 && homeFlagSafe) {
                        enemy.score++;
                        team.carrier = null;
                        carrier.setGlowing(false);
                        broadcast(teams, enemy.colour + carrier.getName() + " &7captured the " + team.colour + team.name + " flag&7! "
                                + "&c" + red.score + " &7- &9" + blue.score);
                        Fx.p(s.world, Particle.FIREWORK, enemy.base.clone().add(0, 2, 0), 60, 0.5, 0.2);
                        s.sound(enemy.base, "ui.toast.challenge_complete", 4f, 1.2f);
                    }
                }
            }
            if (t % 10 == 0) {
                bar.name(s.text("&c&lRED " + red.score + " &7- &9&l" + blue.score + " BLUE &8| &e" + Math.max(0, left) + "s &8| &7first to " + win));
                bar.progress(Math.max(0, Math.min(1, left / (float) secs)));
                for (Player p : s.world.getPlayers()) {
                    boolean near = p.getLocation().distance(s.at()) <= d("range") + dist;
                    if (near && shown.add(p)) p.showBossBar(bar);
                    else if (!near && shown.remove(p)) p.hideBossBar(bar);
                }
            }
            Team winner = red.score >= win ? red : blue.score >= win ? blue : null;
            if (winner == null && left > 0) return true;
            if (winner == null && red.score != blue.score) winner = red.score > blue.score ? red : blue;
            for (Player p : shown) p.hideBossBar(bar);
            for (Team team : teams) {
                team.flag.remove();
                Player c = team.carrier == null ? null : Bukkit.getPlayer(team.carrier);
                if (c != null) c.setGlowing(false);
            }
            if (winner == null) {
                broadcast(teams, "&7Time's up - it's a &fdraw&7!");
                return false;
            }
            for (UUID id : winner.members) {
                Player p = Bukkit.getPlayer(id);
                if (p != null) s.give(p, list("prize"));
            }
            for (Team team : teams) for (UUID id : team.members) {
                Player p = Bukkit.getPlayer(id);
                if (p != null) Fx.title(p, winner.colour + "&l" + winner.name.toUpperCase() + " WINS!", "&c" + red.score + " &7- &9" + blue.score, 80);
            }
            return false;
        });
    }

    private static void broadcast(Team[] teams, String msg) {
        for (Team t : teams) for (UUID id : t.members) {
            Player p = Bukkit.getPlayer(id);
            if (p != null) p.sendMessage(MegaOrbitals.color("&8[&dCTF&8] " + msg));
        }
    }
}
