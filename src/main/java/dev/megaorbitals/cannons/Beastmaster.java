package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;

/** Every tameable animal nearby becomes yours: wolves, cats, parrots, horses. Plus a few new wolves. */
public final class Beastmaster extends Cannon {
    public Beastmaster() {
        super("&6&lBeastmaster", Material.LEAD, "Every tameable animal nearby becomes yours:", "wolves, cats, parrots, horses. Plus new wolves.");
        warning(10, "#C08040");
        noBlockDamage();
        option("radius", 30, "Range.");
        option("steal-from-others", true, "Also take animals tamed by other players.");
        option("bonus-wolves", 3, "Extra wolves summoned for you.");
    }

    @Override
    public void strike(Strike s) {
        Player me = s.shooter;
        if (me == null) return;
        int count = 0;
        double r = d("radius");
        for (Entity e : s.world.getNearbyEntities(s.at(), r, r, r)) {
            if (!(e instanceof Tameable t)) continue;
            if (t.isTamed() && !b("steal-from-others")) continue;
            t.setTamed(true);
            t.setOwner(me);
            if (t instanceof Sittable st) st.setSitting(false);
            Fx.p(s.world, Particle.HEART, e.getLocation().add(0, 1, 0), 5, 0.3, 0);
            count++;
        }
        for (int k = 0; k < i("bonus-wolves"); k++) {
            s.mob(s.randomPoint(3), Wolf.class, w -> { w.setTamed(true); w.setOwner(me); });
        }
        s.actionBar(me, "&6You now command &f" + (count + i("bonus-wolves")) + " &6animals.");
        s.sound(s.at(), "entity.wolf.howl", 8f, 1f);
    }
}
