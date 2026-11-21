package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;

/** A pillar of spinning fire that drags everyone into its burning core. */
public final class Firenado extends Cannon {
    public Firenado() {
        super("&6&lFirenado", Material.BLAZE_ROD, "A spinning pillar of fire that drags", "everyone into its burning core.");
        warning(30, "#FF6000");
        option("seconds", 15, "How long it burns.");
        option("pull-radius", 10.0, "How far it pulls.");
        option("damage", 3.0, "Damage every second inside it.");
        option("height", 25, "Height of the fire column.");
    }

    @Override
    public void strike(Strike s) {
        Location c = s.at();
        int h = i("height");
        s.every(1, i("seconds") * 20, t -> {
            for (int y = 0; y < h; y += 1) {
                double rad = 0.8 + y * 0.18;
                double a = t * 0.6 + y * 0.5;
                Fx.p(s.world, Particle.FLAME, c.clone().add(Math.cos(a) * rad, y, Math.sin(a) * rad), 1, 0.05, 0.01);
                Fx.p(s.world, Particle.FLAME, c.clone().add(-Math.cos(a) * rad, y, -Math.sin(a) * rad), 1, 0.05, 0.01);
            }
            for (LivingEntity le : s.victims(c, d("pull-radius"))) {
                Vector flat = c.toVector().subtract(le.getLocation().toVector()).setY(0);
                if (flat.lengthSquared() < 0.01) flat = new Vector(1, 0, 0);
                Vector spin = new Vector(-flat.getZ(), 0, flat.getX()).normalize().multiply(0.5);
                le.setVelocity(spin.add(flat.normalize().multiply(0.25)).setY(0.25));
                le.setFireTicks(100);
                if (t % 20 == 0) s.damage(le, d("damage"));
            }
            if (s.breaks() && t % 5 == 0) {
                Block b = s.randomPoint(6).getBlock();
                if (b.getType().isAir()) Blocks.put(b, Material.FIRE);
            }
            if (t % 15 == 0) s.sound(c, "entity.blaze.burn", 6f, 0.5f);
        });
    }
}
