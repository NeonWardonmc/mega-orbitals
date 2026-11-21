package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;

/** Summons a real Ender Dragon over the target. It leaves after a few minutes (if it survives). */
public final class DragonSummon extends Cannon {
    public DragonSummon() {
        super("&5&lSummon the Dragon", Material.DRAGON_EGG, "Summons a real Ender Dragon over the target.", "It flies away after a few minutes.");
        warning(100, "#8000FF");
        option("minutes", 3, "How long the dragon stays.");
        option("height", 30, "Spawn height.");
    }

    @Override
    public void strike(Strike s) {
        EnderDragon dragon = s.world.spawn(s.at(0, i("height"), 0), EnderDragon.class, dr -> dr.setPhase(EnderDragon.Phase.CIRCLING));
        s.tag(dragon);
        s.soundWorld("entity.ender_dragon.growl", 2f, 1f);
        s.later(i("minutes") * 1200L, () -> {
            if (!dragon.isValid()) return;
            Fx.p(s.world, Particle.PORTAL, dragon.getLocation(), 300, 4, 1);
            dragon.remove();
            s.soundWorld("entity.ender_dragon.ambient", 1f, 0.6f);
        });
    }
}
