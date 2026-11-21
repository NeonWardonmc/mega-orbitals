package dev.megaorbitals;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/** Messages from lang/<language>.yml, falling back to the bundled English file. */
public final class Lang {

    private final MegaOrbitals plugin;
    private YamlConfiguration messages = new YamlConfiguration();

    public Lang(MegaOrbitals plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        File en = new File(plugin.getDataFolder(), "lang/en.yml");
        if (!en.exists()) plugin.saveResource("lang/en.yml", false);
        String code = plugin.getConfig().getString("language", "en");
        File f = new File(plugin.getDataFolder(), "lang/" + code + ".yml");
        if (!f.exists()) {
            plugin.getLogger().warning("Language file lang/" + code + ".yml not found, using English.");
            f = en;
        }
        YamlConfiguration y = YamlConfiguration.loadConfiguration(f);
        var bundled = plugin.getResource("lang/en.yml");
        if (bundled != null) y.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(bundled, StandardCharsets.UTF_8)));
        messages = y;
    }

    /** A message by key. Old config.yml "messages:" entries still work as a fallback. */
    public String get(String key) {
        String v = messages.getString(key);
        if (v == null) v = plugin.getConfig().getString("messages." + key);
        return v == null ? key : v;
    }

    public String prefixed(String key) {
        return get("prefix") + get(key);
    }
}
