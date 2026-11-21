package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

/** YOU are the payload: you're launched into orbit and slam down on the target in a drop pod. */
public final class DropPod extends Cannon {
    public DropPod() {
        super("&c&lDrop Pod", Material.IRON_TRAPDOOR, "YOU are the payload. You go up to orbit", "and slam down on the target in a drop pod.");
        warning(10, "#FF6040");
        option("height", 80, "Drop height.");
        option("impact-damage", 14.0, "Damage to everyone near the landing.");
        option("impact-radius", 8.0, "Size of the landing shockwave.");
        option("power", 4.0, "Explosion power of the landing (does not hurt you).");
    }

    @Override
    public void strike(Strike s) {
        Player p = s.shooter;
        if (p == null || !p.isOnline()) return;
        Location sky = s.at(0, i("height"), 0);
        sky.setDirection(new Vector(0, -1, 0));
        p.teleport(sky);
        p.getPersistentDataContainer().set(s.plugin.noFallKey, PersistentDataType.BYTE, (byte) 1);
        p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 100, 4));
        BlockDisplay pod = Fx.display(sky, Material.IRON_BLOCK, 1.6f, 2.6f, 1.6f);
        s.loop(1, t -> {
            if (!p.isOnline() || t > 300) { pod.remove(); return false; }
            p.setVelocity(new Vector(0, -2.5, 0));
            pod.teleport(p.getLocation().add(0, 1, 0));
            Fx.p(s.world, Particle.FLAME, p.getLocation().add(0, 3, 0), 10, 0.4, 0.05);
            Fx.p(s.world, Particle.LARGE_SMOKE, p.getLocation().add(0, 4, 0), 5, 0.4, 0.05);
            if (!p.isOnGround()) return true;
            pod.remove();
            Location l = p.getLocation();
            s.world.createExplosion(l, (float) d("power"), false, s.breaks(), p);
            for (LivingEntity le : s.victims(l, d("impact-radius"))) {
                s.damage(le, d("impact-damage"));
                le.setVelocity(Fx.dir(l, le.getLocation()).multiply(1.5).setY(0.8));
            }
            s.sound(l, "item.mace.smash_ground_heavy", 10f, 0.5f);
            return false;
        });
        s.sound(sky, "entity.firework_rocket.launch", 4f, 0.5f);
    }
}
