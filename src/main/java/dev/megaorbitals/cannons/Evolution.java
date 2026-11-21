package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

/** Forced evolution: every mob nearby upgrades to a stronger form. Pigs become hoglins, skeletons become wither skeletons... */
public final class Evolution extends Cannon {
    public Evolution() {
        super("&5&lForced Evolution", Material.EXPERIENCE_BOTTLE, "Every mob nearby evolves into something worse.", "Pigs become hoglins, skeletons go wither...");
        warning(20, "#A040C0");
        option("radius", 25, "Range.");
    }

    private Class<? extends LivingEntity> evolve(LivingEntity le) {
        if (le instanceof Pig) return Hoglin.class;
        if (le instanceof Cow) return Ravager.class;
        if (le instanceof Sheep) return Evoker.class;
        if (le instanceof Villager) return Vindicator.class;
        if (le instanceof Chicken) return Phantom.class;
        if (le instanceof Skeleton) return WitherSkeleton.class;
        if (le instanceof Spider && !(le instanceof CaveSpider)) return CaveSpider.class;
        if (le instanceof Slime && !(le instanceof MagmaCube)) return MagmaCube.class;
        if (le instanceof Silverfish) return Endermite.class;
        if (le instanceof Wolf w && !w.isTamed()) return Zoglin.class;
        return null;
    }

    @Override
    public void strike(Strike s) {
        for (LivingEntity le : s.victims(s.at(), d("radius"))) {
            if (le instanceof Player) continue;
            Location l = le.getLocation();
            Fx.p(s.world, Particle.WITCH, l.clone().add(0, 1, 0), 30, 0.5, 0.1);
            if (le instanceof Creeper c) { c.setPowered(true); continue; }
            if (le instanceof Zombie z && !(le instanceof PigZombie)) {
                EntityEquipment eq = z.getEquipment();
                eq.setHelmet(new ItemStack(Material.DIAMOND_HELMET));
                eq.setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
                eq.setItemInMainHand(new ItemStack(Material.DIAMOND_SWORD));
                eq.setHelmetDropChance(0); eq.setChestplateDropChance(0); eq.setItemInMainHandDropChance(0);
                continue;
            }
            Class<? extends LivingEntity> next = evolve(le);
            if (next == null) continue;
            le.remove();
            s.hunt(s.mob(l, next, null), 40);
        }
        s.sound(s.at(), "entity.zombie_villager.converted", 8f, 0.6f);
    }
}
