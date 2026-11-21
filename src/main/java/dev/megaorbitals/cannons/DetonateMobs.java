package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import java.util.*;

/** Every mob nearby starts to swell and glow... then they all explode. */
public final class DetonateMobs extends Cannon {
    public DetonateMobs() {
        super("&c&lLiving Bombs", Material.TNT, "Every mob nearby starts to swell and glow...", "then they all explode.");
        warning(15, "#FF2020");
        option("radius", 30, "Range.");
        option("fuse-seconds", 3, "Time before they explode.");
        option("power", 3.0, "Explosion power per mob.");
    }

    @Override
    public void strike(Strike s) {
        List<LivingEntity> bombs = new ArrayList<>();
        for (LivingEntity le : s.victims(s.at(), d("radius"))) if (!(le instanceof Player)) bombs.add(le);
        int fuse = i("fuse-seconds") * 20;
        s.every(2, fuse / 2, t -> {
            for (LivingEntity le : bombs) {
                if (!le.isValid()) continue;
                le.setGlowing(true);
                Fx.p(s.world, Particle.SMOKE, le.getLocation().add(0, le.getHeight(), 0), 3, 0.2, 0.02);
                if (t % 5 == 0) s.sound(le.getLocation(), "entity.creeper.primed", 1f, 1f);
            }
        });
        s.later(fuse, () -> {
            for (LivingEntity le : bombs) {
                if (!le.isValid()) continue;
                Location l = le.getLocation();
                le.remove();
                s.explode(l, (float) d("power"), false);
            }
        });
    }
}
