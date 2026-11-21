package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;

/** A storm of homing shulker bullets: everyone they hit floats helplessly into the air. */
public final class ShulkerSwarm extends Cannon {
    public ShulkerSwarm() {
        super("&d&lShulker Storm", Material.SHULKER_SHELL, "Homing shulker bullets chase everyone.", "Each hit makes you float helplessly.");
        warning(20, "#D080FF");
        option("radius", 30, "Range.");
        option("bullets-per-target", 6, "Bullets fired at each target.");
    }

    @Override
    public void strike(Strike s) {
        Location sky = s.at(0, 20, 0);
        s.every(5, i("bullets-per-target"), t -> {
            for (LivingEntity le : s.victims(s.at(), d("radius"))) {
                ShulkerBullet b = s.world.spawn(sky.clone().add(Math.random() * 6 - 3, 0, Math.random() * 6 - 3), ShulkerBullet.class, x -> {
                    x.setTarget(le);
                    if (s.shooter != null) x.setShooter(s.shooter);
                });
                s.expire(s.tag(b), 300);
            }
            s.sound(sky, "entity.shulker.shoot", 6f, 1f);
        });
    }
}
