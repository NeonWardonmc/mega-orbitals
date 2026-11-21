package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.util.Vector;
import java.util.*;

/** The ground fractures: jagged cracks race out from the impact, carving tunnels in random directions. */
public final class Fracture extends Cannon {
    public Fracture() {
        super("&8&lFracture", Material.CRACKED_STONE_BRICKS, "The ground fractures: jagged cracks race out", "from the impact, carving twisting tunnels.");
        warning(25, "#707070");
        option("cracks", 8, "Number of cracks.");
        option("length", 45, "Length of each crack.");
        option("width", 1.6, "Crack width.");
    }

    @Override
    public void strike(Strike s) {
        Random rnd = new Random();
        double w = d("width");
        for (int k = 0; k < i("cracks"); k++) {
            Location pos = s.at(0, -1, 0);
            Vector dir = new Vector(1, 0, 0).rotateAroundY(rnd.nextDouble() * Math.PI * 2).setY(-0.3 + rnd.nextDouble() * 0.2);
            s.every(1, i("length"), t -> {
                dir.add(new Vector(rnd.nextGaussian() * 0.3, rnd.nextGaussian() * 0.15, rnd.nextGaussian() * 0.3)).normalize();
                if (dir.getY() > 0.2) dir.setY(0.2);
                pos.add(dir);
                if (s.breaks()) {
                    int iw = (int) Math.ceil(w);
                    for (int x = -iw; x <= iw; x++) for (int y = -iw; y <= iw; y++) for (int z = -iw; z <= iw; z++) {
                        if (x * x + y * y + z * z <= w * w) Blocks.set(pos.clone().add(x, y, z).getBlock(), Material.AIR);
                    }
                }
                Fx.block(s.world, Particle.BLOCK, pos, Material.STONE, 6, 0.5);
                if (t % 6 == 0) s.sound(pos, "block.stone.break", 2f, 0.5f);
            });
        }
    }
}
