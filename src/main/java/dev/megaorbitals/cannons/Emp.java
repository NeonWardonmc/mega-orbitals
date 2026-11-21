package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/** An electromagnetic pulse: fries redstone and locks everyone's gadgets. */
public final class Emp extends Cannon {
    public Emp() {
        super("&e&lEMP", Material.REDSTONE_BLOCK, "Fries all redstone in range and locks", "elytra, pearls, rockets, shields and tridents.");
        warning(30, "#FFFF40");
        option("radius", 40, "Pulse radius.");
        option("lockout-seconds", 30, "How long gadgets are locked.");
        option("fry-redstone", true, "Destroy redstone components in range.");
    }

    private static final Material[] LOCKED = {Material.FIREWORK_ROCKET, Material.ENDER_PEARL, Material.SHIELD, Material.TRIDENT,
            Material.WIND_CHARGE, Material.CHORUS_FRUIT, Material.ELYTRA, Material.CROSSBOW, Material.BOW, Material.TOTEM_OF_UNDYING};

    private static boolean redstone(Material m) {
        String n = m.name();
        return n.contains("REDSTONE") || n.contains("REPEATER") || n.contains("COMPARATOR") || n.contains("OBSERVER")
                || n.contains("PISTON") || n.equals("DAYLIGHT_DETECTOR") || n.contains("PRESSURE_PLATE") || n.equals("LEVER")
                || n.equals("SCULK_SENSOR") || n.equals("TARGET") || n.contains("BUTTON");
    }

    @Override
    public void strike(Strike s) {
        Location c = s.at(0, 1, 0);
        double r = d("radius");
        int lock = i("lockout-seconds") * 20;
        s.every(1, 20, t -> Fx.sphere(s.world, Particle.ELECTRIC_SPARK, c, r * (t + 1) / 20.0, 400));
        s.sound(c, "block.beacon.deactivate", 10f, 2f);
        s.sound(c, "entity.lightning_bolt.impact", 10f, 2f);
        for (LivingEntity le : s.victims(c, r)) {
            le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 200, 1));
            le.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, lock, 2));
            le.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 0));
            if (le instanceof Player p) {
                p.setGliding(false);
                for (Material m : LOCKED) p.setCooldown(m, lock);
                Fx.title(p, "&e&lSYSTEMS OFFLINE", "&7EMP blast. Gadgets locked.", 40);
            }
        }
        if (b("fry-redstone")) {
            int ir = (int) r;
            s.blocks(Blocks.box(c, ir, Math.min(ir, 30), ir, (b, dx, dy, dz) -> {
                if (redstone(b.getType())) Blocks.set(b, Material.AIR);
            }));
        }
    }
}
