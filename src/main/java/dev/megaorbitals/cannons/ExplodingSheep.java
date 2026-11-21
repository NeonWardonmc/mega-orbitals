package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;

/** A flock of adorable rainbow sheep wanders in... each one is packed with explosives. */
public final class ExplodingSheep extends Cannon {
    public ExplodingSheep() {
        super("&d&lExploding Sheep", Material.PINK_WOOL, "A flock of adorable rainbow sheep...", "each one packed with explosives.");
        warning(15, "#FF80C0");
        option("sheep", 12, "Number of sheep.");
        option("radius", 8.0, "Spread.");
        option("fuse-seconds", 6, "Time before they blow.");
        option("power", 3.0, "Explosion power per sheep.");
    }

    @Override
    public void strike(Strike s) {
        for (Location l : Fx.circle(s.at(), d("radius"), i("sheep"))) {
            Sheep sh = s.mob(s.surface(l), Sheep.class, x -> {
                x.customName(s.text("jeb_"));
                x.setCustomNameVisible(false);
            });
            s.every(10, i("fuse-seconds") * 2, t -> {
                if (!sh.isValid()) return;
                if (t % 2 == 0) s.sound(sh.getLocation(), "entity.sheep.ambient", 1.5f, 1.5f);
                Fx.p(s.world, Particle.SMOKE, sh.getLocation().add(0, 1, 0), 2, 0.2, 0.01);
            });
            s.later(i("fuse-seconds") * 20L, () -> {
                if (!sh.isValid()) return;
                Location at = sh.getLocation();
                sh.remove();
                s.explode(at, (float) d("power"), false);
            });
        }
    }
}
