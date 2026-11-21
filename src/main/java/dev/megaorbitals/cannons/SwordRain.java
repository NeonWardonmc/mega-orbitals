package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;
import java.util.*;

/** Giant swords rain point-first from the sky and stick in the ground where they land. */
public final class SwordRain extends Cannon {
    public SwordRain() {
        super("&f&lRain of Swords", Material.DIAMOND_SWORD, "Giant swords rain point-first from the sky", "and stick in the ground where they land.");
        warning(25, "#E0F0FF");
        noBlockDamage();
        option("radius", 18, "Area covered.");
        option("swords", 40, "Number of swords.");
        option("damage", 12.0, "Damage when a sword hits you.");
        option("aimed-fraction", 0.5, "Share of swords aimed straight at targets (0-1).");
    }

    @Override
    public void strike(Strike s) {
        Random rnd = new Random();
        Material[] kinds = {Material.IRON_SWORD, Material.DIAMOND_SWORD, Material.NETHERITE_SWORD, Material.GOLDEN_SWORD};
        s.every(2, i("swords"), t -> {
            List<LivingEntity> targets = s.victims(s.at(), d("radius"));
            Location hit = !targets.isEmpty() && rnd.nextDouble() < d("aimed-fraction")
                    ? targets.get(rnd.nextInt(targets.size())).getLocation() : s.randomPoint(d("radius"));
            Location top = hit.clone().add(0, 30, 0);
            ItemDisplay sword = Fx.itemDisplay(top, kinds[rnd.nextInt(kinds.length)], 2.5f);
            sword.setTransformation(new Transformation(new Vector3f(), new AxisAngle4f((float) Math.toRadians(135), 0, 0, 1),
                    new Vector3f(2.5f, 2.5f, 2.5f), new AxisAngle4f()));
            s.every(1, 11, k -> {
                Location p = top.clone().subtract(0, 30 * (k + 1) / 11.0, 0).add(0, 1.2, 0);
                sword.teleport(p);
                if (k != 10) return;
                for (LivingEntity le : s.victims(hit, 1.8)) s.damage(le, d("damage"));
                Fx.block(s.world, Particle.BLOCK, hit, Material.DIRT, 12, 0.3);
                s.sound(hit, "item.trident.hit_ground", 3f, 0.6f);
                s.later(100, sword::remove);
            });
        });
    }
}
