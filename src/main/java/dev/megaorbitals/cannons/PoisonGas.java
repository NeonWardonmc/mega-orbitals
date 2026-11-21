package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import java.util.*;

/** Toxic gas clouds that drift with the wind, spreading wherever it blows. */
public final class PoisonGas extends Cannon {
    public PoisonGas() {
        super("&2&lGas Attack", Material.GREEN_DYE, "Toxic gas clouds that drift with the wind.", "Watch which way it blows.");
        warning(20, "#60A040");
        noBlockDamage();
        option("clouds", 10, "Number of gas clouds.");
        option("seconds", 40, "How long the gas lasts.");
        option("wind-speed", 0.08, "How fast the clouds drift.");
    }

    @Override
    public void strike(Strike s) {
        Vector wind = new Vector(1, 0, 0).rotateAroundY(Math.random() * Math.PI * 2).multiply(d("wind-speed"));
        List<AreaEffectCloud> clouds = new ArrayList<>();
        for (int k = 0; k < i("clouds"); k++) {
            Location l = s.randomPoint(8);
            clouds.add(s.tag(s.world.spawn(l, AreaEffectCloud.class, c -> {
                c.setRadius(4f);
                c.setRadiusPerTick(0f);
                c.setDuration(i("seconds") * 20);
                c.setWaitTime(0);
                c.setReapplicationDelay(20);
                c.setColor(Color.fromRGB(80, 160, 40));
                c.addCustomEffect(new PotionEffect(PotionEffectType.POISON, 100, 2), true);
                c.addCustomEffect(new PotionEffect(PotionEffectType.WITHER, 60, 0), true);
                c.addCustomEffect(new PotionEffect(PotionEffectType.NAUSEA, 120, 0), true);
            })));
        }
        s.every(2, i("seconds") * 10, t -> {
            if (t % 50 == 0) wind.rotateAroundY((Math.random() - 0.5) * 0.8);
            for (AreaEffectCloud c : clouds) {
                if (!c.isValid()) continue;
                Location to = s.surface(c.getLocation().add(wind.clone().multiply(2)));
                c.teleport(to);
            }
        });
        s.sound(s.at(), "block.fire.extinguish", 8f, 0.5f);
    }
}
