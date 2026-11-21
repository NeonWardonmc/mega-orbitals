package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;

/** Floods a bowl of water and spins it into a whirlpool that drags everyone under. */
public final class Whirlpool extends Cannon {
    public Whirlpool() {
        super("&3&lWhirlpool", Material.NAUTILUS_SHELL, "Floods a bowl of water and spins it into", "a whirlpool that drags everyone under.");
        warning(30, "#2060A0");
        option("radius", 11, "Size of the whirlpool.");
        option("depth", 7, "Depth of the water.");
        option("seconds", 15, "How long it spins.");
        option("core-damage", 3.0, "Damage every second at the centre.");
    }

    @Override
    public void strike(Strike s) {
        int r = i("radius"), depth = i("depth");
        Location c = s.at();
        s.blocks(Blocks.box(c.clone().add(0, -depth / 2.0, 0), r, depth / 2 + 1, r, (b, dx, dy, dz) -> {
            double dist = Math.sqrt(dx * dx + dz * dz);
            int floor = (int) (-depth * (1 - dist / r));
            if (dist <= r && b.getY() >= c.getBlockY() + floor && b.getY() < c.getBlockY()) Blocks.set(b, Material.WATER);
        }));
        s.every(1, i("seconds") * 20, t -> {
            for (LivingEntity le : s.victims(c, r + 2)) {
                Vector flat = c.toVector().subtract(le.getLocation().toVector()).setY(0);
                if (flat.lengthSquared() < 0.01) flat = new Vector(0.1, 0, 0);
                Vector spin = new Vector(-flat.getZ(), 0, flat.getX()).normalize().multiply(0.35);
                le.setVelocity(spin.add(flat.normalize().multiply(0.12)).setY(-0.25));
                if (flat.length() < 2.5 && t % 20 == 0) s.damage(le, d("core-damage"));
            }
            for (double rr = 1; rr < r; rr += 2) {
                double a = t * 0.2 + rr;
                Fx.p(s.world, Particle.BUBBLE_POP, c.clone().add(Math.cos(a) * rr, -0.5, Math.sin(a) * rr), 2, 0.2, 0);
            }
            if (t % 20 == 0) s.sound(c, "block.bubble_column.whirlpool_ambient", 6f, 0.6f);
        });
    }
}
