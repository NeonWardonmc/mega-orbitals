package dev.megaorbitals;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Checks the GitHub repo's latest release and tells ops (megaorbitals.update) when a newer version is out. */
public final class UpdateChecker implements Listener {

    private static final Pattern TAG = Pattern.compile("\"tag_name\"\\s*:\\s*\"v?([^\"]+)\"");
    private final MegaOrbitals plugin;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private volatile String latest;

    public UpdateChecker(MegaOrbitals plugin) {
        this.plugin = plugin;
    }

    public void start() {
        if (!plugin.getConfig().getBoolean("update-checker.enabled", true)) return;
        long hours = Math.max(1, plugin.getConfig().getLong("update-checker.every-hours", 6));
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, this::check, 100L, hours * 72000L);
    }

    private String repo() { return plugin.getConfig().getString("update-checker.repo", "NeonWardonmc/mega-orbitals"); }

    private void check() {
        try {
            HttpRequest req = HttpRequest.newBuilder(URI.create("https://api.github.com/repos/" + repo() + "/releases/latest"))
                    .timeout(Duration.ofSeconds(10)).header("Accept", "application/vnd.github+json")
                    .header("User-Agent", "MegaOrbitals-UpdateChecker").GET().build();
            HttpResponse<String> r = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (r.statusCode() != 200) return;
            Matcher m = TAG.matcher(r.body());
            if (!m.find()) return;
            String found = m.group(1);
            String current = plugin.getPluginMeta().getVersion();
            if (newer(found, current) && !found.equals(latest)) {
                latest = found;
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    plugin.getLogger().info("A new version is available: " + found + " (you have " + current + ") " + url());
                    for (Player p : plugin.getServer().getOnlinePlayers()) notify(p);
                });
            }
        } catch (Exception ignored) {
            // offline or rate-limited: try again next time
        }
    }

    private String url() { return "https://github.com/" + repo() + "/releases/latest"; }

    private void notify(Player p) {
        if (latest == null || !p.hasPermission("megaorbitals.update")) return;
        p.sendMessage(MegaOrbitals.color(plugin.settings().prefixed("update-available").replace("{latest}", latest)
                .replace("{current}", plugin.getPluginMeta().getVersion()).replace("{url}", url())));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        notify(e.getPlayer());
    }

    /** True if version a is greater than b, comparing numbers dot by dot (1.10.0 > 1.9.2). */
    static boolean newer(String a, String b) {
        String[] x = a.split("[.-]"), y = b.split("[.-]");
        for (int i = 0; i < Math.max(x.length, y.length); i++) {
            int p = i < x.length ? num(x[i]) : 0, q = i < y.length ? num(y[i]) : 0;
            if (p != q) return p > q;
        }
        return false;
    }

    private static int num(String s) {
        try { return Integer.parseInt(s.replaceAll("\\D", "")); } catch (NumberFormatException e) { return 0; }
    }
}
