package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;

/** A building-sized anvil falls from orbit and pounds the target flat. */
public final class OrbitalHammer extends Cannon {
    public OrbitalHammer() {
        super("&8&lOrbital Hammer", Material.ANVIL, "A building-sized anvil falls from orbit", "and pounds the target flat.");
        warning(40, "#505050");
        option("size", 9.0, "Size of the anvil.");
        option("damage", 50.0, "Damage to anything crushed.");
        option("crush-depth", 4, "How deep it pushes the ground down.");
    }

    @Override
    public void strike(Strike s) {
        double size = d("size");
        Location start = s.at(0, 100, 0);
        BlockDisplay anvil = Fx.display(start, Material.ANVIL, (float) size);
        int fall = 40;
        s.every(1, fall + 1, t -> {
            double p = Math.pow(t / (double) fall, 2);
            Location l = start.clone().subtract(0, (100 - size / 2) * p, 0);
            if (t % 2 == 0 || t == fall) anvil.teleport(l);
            if (t < fall) return;
            Location c = s.at();
            for (LivingEntity le : s.victims(c, size)) {
                s.damage(le, d("damage"));
                le.setVelocity(Fx.dir(c, le.getLocation()).multiply(2).setY(0.6));
            }
            int r = (int) (size / 2) + 1, depth = i("crush-depth");
            s.blocks(Blocks.columns(c, r, (x, z, dist) -> {
                Block top = s.surface(new Location(s.world, x + 0.5, c.getY(), z + 0.5)).getBlock();
                for (int y = 0; y < depth; y++) Blocks.set(top.getRelative(0, -1 - y, 0), Material.AIR);
                return depth;
            }));
            Fx.p(s.world, Particle.EXPLOSION_EMITTER, c, 3, size / 3, 0);
            s.soundWorld("block.anvil.land", 1f, 0.3f);
            s.later(60, anvil::remove);
        });
    }
}
