package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;
import java.util.*;

/** Giant boulders crash down and roll outward, flattening everything in their path. */
public final class Boulders extends Cannon {
    public Boulders() {
        super("&7&lRolling Boulders", Material.COBBLESTONE, "Giant boulders crash down and roll outward,", "flattening everything in their path.");
        warning(25, "#909090");
        option("boulders", 6, "Number of boulders.");
        option("distance", 40, "How far they roll.");
        option("damage", 14.0, "Damage when run over.");
        option("size", 4.0, "Boulder size.");
    }

    @Override
    public void strike(Strike s) {
        int n = i("boulders");
        double size = d("size");
        for (int k = 0; k < n; k++) {
            Vector dir = new Vector(1, 0, 0).rotateAroundY(2 * Math.PI * k / n);
            BlockDisplay rock = Fx.display(s.at(0, size / 2, 0), Material.COBBLESTONE, (float) size);
            Set<UUID> hit = new HashSet<>();
            Location pos = s.at();
            s.every(1, i("distance") * 2, t -> {
                pos.add(dir.clone().multiply(0.5));
                Location g = s.surface(pos).add(0, size / 2, 0);
                pos.setY(g.getY() - size / 2);
                if (t % 2 == 0) rock.teleport(g);
                Fx.block(s.world, Particle.BLOCK, g.clone().subtract(0, size / 2, 0), Material.DIRT, 6, 0.8);
                for (LivingEntity le : s.victims(g, size / 2 + 1)) {
                    if (!hit.add(le.getUniqueId())) continue;
                    s.damage(le, d("damage"));
                    le.setVelocity(dir.clone().multiply(1.2).setY(0.6));
                }
                if (s.breaks() && t % 2 == 0) {
                    int rr = (int) (size / 2);
                    for (int x = -rr; x <= rr; x++) for (int y = 1; y <= rr * 2; y++) for (int z = -rr; z <= rr; z++) {
                        Blocks.set(pos.clone().add(x, y, z).getBlock(), Material.AIR);
                    }
                }
                if (t % 8 == 0) s.sound(g, "block.stone.break", 3f, 0.5f);
                if (t == i("distance") * 2 - 1) rock.remove();
            });
        }
    }
}
