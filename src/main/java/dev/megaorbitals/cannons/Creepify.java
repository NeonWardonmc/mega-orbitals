package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;

/** Every mob nearby is turned into a creeper that hunts the nearest player. */
public final class Creepify extends Cannon {
    public Creepify() {
        super("&a&lCreepify", Material.GUNPOWDER, "Every mob nearby turns into a creeper", "that hunts the nearest player.");
        warning(15, "#40C040");
        option("radius", 25, "Range.");
        option("charged", false, "Make them charged creepers.");
        option("include-passive", true, "Also convert cows, pigs, villagers...");
    }

    @Override
    public void strike(Strike s) {
        for (LivingEntity le : s.victims(s.at(), d("radius"))) {
            if (le instanceof Player || le instanceof Creeper || le instanceof ArmorStand) continue;
            if (!(le instanceof Monster) && !b("include-passive")) continue;
            if (le instanceof Wither || le instanceof Warden || le instanceof EnderDragon) continue;
            Location l = le.getLocation();
            le.remove();
            Creeper c = s.mob(l, Creeper.class, cr -> cr.setPowered(b("charged")));
            s.hunt(c, 40);
            Fx.p(s.world, Particle.HAPPY_VILLAGER, l.clone().add(0, 1, 0), 10, 0.4, 0);
        }
        s.sound(s.at(), "entity.creeper.primed", 8f, 0.6f);
    }
}
