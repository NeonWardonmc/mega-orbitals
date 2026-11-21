package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/** A black sun blots out the sky: it becomes midnight and the creatures of the night pour in. */
public final class Eclipse extends Cannon {
    public Eclipse() {
        super("&8&lEclipse", Material.BLACK_CONCRETE, "A black sun blots out the sky. It becomes", "midnight and creatures of the night pour in.");
        warning(40, "#101010");
        option("change-time", true, "Set the world to midnight.");
        option("radius", 25, "Range.");
        option("zombies", 8, "Zombies.");
        option("skeletons", 6, "Skeletons.");
        option("phantoms", 4, "Phantoms.");
        option("darkness-seconds", 30, "Darkness for everyone in range.");
    }

    @Override
    public void strike(Strike s) {
        if (b("change-time")) s.world.setTime(18000);
        BlockDisplay sun = Fx.display(s.at(0, 60, 0), Material.BLACK_CONCRETE, 14f, 14f, 1f);
        s.later(i("darkness-seconds") * 20L, sun::remove);
        double r = d("radius");
        for (LivingEntity le : s.victims(s.at(), r)) le.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, i("darkness-seconds") * 20, 0));
        for (int k = 0; k < i("zombies"); k++) s.hunt(s.mob(s.randomPoint(r), Zombie.class, z -> z.setShouldBurnInDay(false)), 50);
        for (int k = 0; k < i("skeletons"); k++) s.hunt(s.mob(s.randomPoint(r), Skeleton.class, z -> z.setShouldBurnInDay(false)), 50);
        for (int k = 0; k < i("phantoms"); k++) s.hunt(s.mob(s.at(0, 20, 0), Phantom.class, p -> p.setShouldBurnInDay(false)), 60);
        s.soundWorld("ambient.cave", 1f, 0.5f);
    }
}
