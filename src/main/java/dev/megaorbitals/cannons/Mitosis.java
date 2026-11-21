package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;

/** Mitosis: every mob nearby splits in two. Then again. */
public final class Mitosis extends Cannon {
    public Mitosis() {
        super("&a&lMitosis", Material.SLIME_BALL, "Every mob nearby splits in two.", "Then again.");
        warning(15, "#60E060");
        option("radius", 20, "Range.");
        option("splits", 2, "How many times they split (2 = 4x the mobs).");
        option("max-mobs", 200, "Safety cap on new mobs.");
    }

    @Override
    public void strike(Strike s) {
        int[] made = {0};
        for (int round = 0; round < i("splits"); round++) {
            s.later(round * 30L, () -> {
                for (LivingEntity le : s.victims(s.at(), d("radius"))) {
                    if (le instanceof Player || le instanceof EnderDragon || le instanceof Wither || made[0] >= i("max-mobs")) continue;
                    EntityType type = le.getType();
                    if (!type.isSpawnable()) continue;
                    Entity copy = s.world.spawnEntity(le.getLocation().add(Math.random() - 0.5, 0, Math.random() - 0.5), type);
                    if (le.customName() != null) copy.customName(le.customName());
                    s.hunt(copy, 40);
                    Fx.p(s.world, Particle.ITEM_SLIME, le.getLocation().add(0, 1, 0), 15, 0.4, 0.05);
                    made[0]++;
                }
                s.sound(s.at(), "entity.slime.squish", 8f, 1.2f);
            });
        }
    }
}
