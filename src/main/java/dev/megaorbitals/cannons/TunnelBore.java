package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.util.Vector;

/** A tunnel-boring beam drills a lit 3x3 tunnel from the target in the direction you face. */
public final class TunnelBore extends Cannon {
    public TunnelBore() {
        super("&6&lTunnel Bore", Material.DIAMOND_PICKAXE, "A boring beam drills a lit 3x3 tunnel from", "the target in the direction you're facing.");
        warning(10, "#C0A060");
        option("length", 80, "Tunnel length.");
        option("radius", 1, "Tunnel radius (1 = 3x3).");
        option("torch-every", 8, "Blocks between torches (0 = none).");
    }

    @Override
    public void strike(Strike s) {
        Vector dir = s.shooter != null ? s.shooter.getLocation().getDirection() : new Vector(1, 0, 0);
        Vector step = dir.normalize();
        int r = i("radius"), te = i("torch-every");
        Location start = s.at(0, 0.5, 0);
        Vector side = new Vector(-step.getZ(), 0, step.getX());
        if (side.lengthSquared() < 0.01) side = new Vector(1, 0, 0);
        Vector sd = side.normalize();
        s.every(1, i("length"), t -> {
            Location c = start.clone().add(step.clone().multiply(t));
            for (int a = -r; a <= r; a++) for (int y = -r; y <= r; y++) {
                Blocks.set(c.clone().add(sd.clone().multiply(a)).add(0, y, 0).getBlock(), Material.AIR);
            }
            if (te > 0 && t % te == 0) {
                var floor = c.clone().add(0, -r - 1, 0).getBlock();
                var torch = c.clone().add(0, -r, 0).getBlock();
                if (floor.getType().isSolid() && torch.getType().isAir()) Blocks.put(torch, Material.TORCH);
            }
            Fx.block(s.world, Particle.BLOCK, c, Material.STONE, 8, 0.8);
            if (t % 5 == 0) s.sound(c, "block.stone.break", 3f, 0.7f);
        });
    }
}
