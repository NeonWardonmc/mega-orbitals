package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;

/** Takes control of the weather: summon a thunderstorm, rain, or clear skies for the whole world. */
public final class WeatherControl extends Cannon {
    public WeatherControl() {
        super("&9&lWeather Control", Material.LIGHTNING_ROD, "Takes control of the weather: summon a", "thunderstorm, rain or clear skies.");
        warning(10, "#6080C0");
        noBlockDamage();
        option("weather", "thunder", "thunder, rain or clear.");
        option("minutes", 10, "How long the weather lasts.");
    }

    @Override
    public void strike(Strike s) {
        int ticks = i("minutes") * 1200;
        String w = str("weather").toLowerCase();
        switch (w) {
            case "clear" -> { s.world.setStorm(false); s.world.setThundering(false); s.world.setClearWeatherDuration(ticks); }
            case "rain" -> { s.world.setStorm(true); s.world.setThundering(false); s.world.setWeatherDuration(ticks); }
            default -> { s.world.setStorm(true); s.world.setThundering(true); s.world.setWeatherDuration(ticks); s.world.setThunderDuration(ticks); }
        }
        s.world.strikeLightningEffect(s.at());
        Fx.dustLine(s.world, s.at(0, 100, 0), s.at(), Color.fromRGB(120, 160, 255), 3f, 1);
        if (s.shooter != null) s.actionBar(s.shooter, "&9Weather set to &f" + w + " &9for " + i("minutes") + " minutes.");
        s.soundWorld("entity.lightning_bolt.thunder", 1f, 0.8f);
    }
}
