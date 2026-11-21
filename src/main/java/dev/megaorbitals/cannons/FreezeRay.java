package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import java.util.*;

/** A freezing beam from you to the target: water turns to ice, lava to obsidian, and everyone in the way freezes solid. */
public final class FreezeRay extends Cannon {
    public FreezeRay() {
        super("&b&lFreeze Ray", Material.ICE, "A freezing beam from you to the target. Water", "becomes ice, lava obsidian, and people freeze.");
        warning(5, "#A0F0FF");
        option("width", 2.5, "Beam width.");
        option("freeze-seconds", 5, "How long targets stay frozen.");
        option("damage", 6.0, "Frost damage.");
    }

    @Override
    public void strike(Strike s) {
        Location from = s.shooter != null && s.shooter.getWorld() == s.world ? s.shooter.getEyeLocation() : s.at(0, 30, 0);
        Location to = s.at();
        Vector d = Fx.dir(from, to);
        double len = from.distance(to), w = d("width");
        Set<UUID> hit = new HashSet<>();
        int fz = i("freeze-seconds") * 20;
        for (double t = 1; t < len; t += 1) {
            Location p = from.clone().add(d.clone().multiply(t));
            Fx.p(s.world, Particle.SNOWFLAKE, p, 3, 0.2, 0.01);
            Fx.dust(s.world, p, Color.fromRGB(180, 240, 255), 1.5f, 1, 0);
            for (LivingEntity le : s.victims(p, w)) {
                if (!hit.add(le.getUniqueId())) continue;
                s.damage(le, d("damage"));
                le.setFreezeTicks(le.getMaxFreezeTicks() + fz);
                le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, fz, 254, false, false));
                le.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, fz, 250, false, false));
                Fx.block(s.world, Particle.BLOCK, le.getLocation().add(0, 1, 0), Material.ICE, 30, 0.4);
            }
            if (!s.breaks()) continue;
            int iw = (int) Math.ceil(w);
            for (int x = -iw; x <= iw; x++) for (int y = -iw; y <= iw; y++) for (int z = -iw; z <= iw; z++) {
                Block b = p.clone().add(x, y, z).getBlock();
                if (b.getType() == Material.WATER) Blocks.set(b, Material.ICE);
                else if (b.getType() == Material.LAVA) Blocks.set(b, Material.OBSIDIAN);
                else if (b.getType() == Material.FIRE) Blocks.set(b, Material.AIR);
            }
        }
        s.sound(from, "block.glass.break", 4f, 1.8f);
        s.sound(to, "block.powder_snow.place", 6f, 0.6f);
    }
}
