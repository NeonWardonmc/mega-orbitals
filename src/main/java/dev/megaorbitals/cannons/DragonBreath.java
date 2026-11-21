package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/** Carpets the whole area in clouds of burning dragon's breath. */
public final class DragonBreath extends Cannon {
    public DragonBreath() {
        super("&5&lDragon's Breath", Material.DRAGON_BREATH, "Carpets the whole area in clouds", "of burning dragon's breath.");
        warning(30, "#B040FF");
        noBlockDamage();
        option("radius", 20, "Area covered.");
        option("seconds", 20, "How long the clouds last.");
        option("damage-level", 1, "Instant damage level of the clouds (0 = I, 1 = II).");
    }

    @Override
    public void strike(Strike s) {
        int r = i("radius");
        s.soundWorld("entity.ender_dragon.growl", 1f, 1f);
        for (int x = -r; x <= r; x += 5) for (int z = -r; z <= r; z += 5) {
            if (x * x + z * z > r * r) continue;
            Location l = s.surface(s.at(x, 0, z));
            s.world.spawn(l, AreaEffectCloud.class, c -> {
                c.setRadius(3.5f);
                c.setRadiusPerTick(0f);
                c.setDuration(i("seconds") * 20);
                c.setWaitTime(0);
                c.setReapplicationDelay(15);
                c.setColor(Color.fromRGB(180, 60, 255));
                c.addCustomEffect(new PotionEffect(PotionEffectType.INSTANT_DAMAGE, 1, i("damage-level")), true);
                if (s.shooter != null) c.setSource(s.shooter);
                s.tag(c);
            });
        }
    }
}
