package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;

/** Totem towers of mobs riding mobs riding mobs: ravager, spider, zombie, skeleton, creeper... */
public final class MobTower extends Cannon {
    public MobTower() {
        super("&2&lMob Totems", Material.ZOMBIE_SPAWN_EGG, "Towers of mobs riding mobs riding mobs:", "ravager, spider, zombie, skeleton, creeper...");
        warning(20, "#40A040");
        option("towers", 3, "Number of mob towers.");
        option("radius", 8.0, "Spread.");
    }

    @Override
    public void strike(Strike s) {
        for (Location l : Fx.circle(s.at(), d("radius"), i("towers"))) {
            Location g = s.surface(l);
            Entity bottom = s.mob(g, Ravager.class, null);
            Entity[] stack = {bottom,
                    s.mob(g, Spider.class, null), s.mob(g, Zombie.class, null),
                    s.mob(g, Skeleton.class, null), s.mob(g, Creeper.class, null), s.mob(g, Chicken.class, null)};
            for (int k = 1; k < stack.length; k++) stack[k - 1].addPassenger(stack[k]);
            for (Entity e : stack) s.hunt(e, 40);
        }
        s.sound(s.at(), "entity.ravager.roar", 8f, 1.2f);
    }
}
