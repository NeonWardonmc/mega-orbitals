package dev.megaorbitals;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

/** Posts to a Discord webhook when big cannons are fired (discord: section of config.yml). */
public final class DiscordAlerts {

    private final MegaOrbitals plugin;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    public DiscordAlerts(MegaOrbitals plugin) {
        this.plugin = plugin;
    }

    public void alert(Cannon c, Location t, Player shooter) {
        var cfg = plugin.getConfig();
        if (!cfg.getBoolean("discord.enabled", false)) return;
        String url = cfg.getString("discord.webhook-url", "");
        if (url.isBlank()) return;
        List<String> tiers = cfg.getStringList("discord.tiers");
        List<String> ids = cfg.getStringList("discord.cannons");
        if (!tiers.contains(c.tier()) && !ids.contains(c.id())) return;

        String text = cfg.getString("discord.message", "**{player}** fired **{cannon}** at {world} {x} {y} {z}")
                .replace("{player}", shooter.getName()).replace("{cannon}", strip(c.name())).replace("{tier}", c.tier())
                .replace("{world}", t.getWorld().getName()).replace("{x}", String.valueOf(t.getBlockX()))
                .replace("{y}", String.valueOf(t.getBlockY())).replace("{z}", String.valueOf(t.getBlockZ()));
        String body = "{\"username\":" + json(cfg.getString("discord.username", "MegaOrbitals")) + ",\"content\":" + json(text) + "}";
        HttpRequest req = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(body)).build();
        http.sendAsync(req, HttpResponse.BodyHandlers.discarding()).thenAccept(r -> {
            if (r.statusCode() >= 300) plugin.getLogger().warning("Discord webhook returned HTTP " + r.statusCode());
        }).exceptionally(ex -> { plugin.getLogger().warning("Discord webhook failed: " + ex.getMessage()); return null; });
    }

    private static String strip(String s) {
        return s.replaceAll("&[0-9a-fk-orA-FK-OR]", "");
    }

    private static String json(String s) {
        StringBuilder b = new StringBuilder("\"");
        for (char ch : s.toCharArray()) {
            switch (ch) {
                case '"' -> b.append("\\\"");
                case '\\' -> b.append("\\\\");
                case '\n' -> b.append("\\n");
                case '\r' -> b.append("\\r");
                case '\t' -> b.append("\\t");
                default -> { if (ch < 0x20) b.append(String.format("\\u%04x", (int) ch)); else b.append(ch); }
            }
        }
        return b.append('"').toString();
    }
}
