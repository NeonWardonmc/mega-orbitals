package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import java.util.*;

/** Raises a snow-capped mountain right out of the ground. */
public final class Terraformer extends Cannon {
    public Terraformer() {
        super("&2&lTerraformer", Material.GRASS_BLOCK, "Raises a snow-capped mountain", "right out of the ground.");
        warning(40, "#40A040");
        option("radius", 30, "Base radius of the mountain.");
        option("height", 45, "Peak height.");
        option("snow-line", 0.7, "Fraction of the height where snow starts.");
    }

    @Override
    public void strike(Strike s) {
        int r = i("radius"), h = i("height");
        World w = s.world;
        int by = s.at().getBlockY() - 1;
        Random rnd = new Random();
        double ox = rnd.nextDouble() * 100, oz = rnd.nextDouble() * 100;
        s.blocks(Blocks.throttle(Blocks.columns(s.at(), r, (x, z, dist) -> {
            double bump = Math.sin((x + ox) * 0.3) * Math.cos((z + oz) * 0.3) * 2;
            int top = (int) (h * Math.pow(1 - dist / r, 1.4) + bump);
            if (top <= 0) return 1;
            for (int y = 1; y <= top; y++) {
                Block b = w.getBlockAt(x, by + y, z);
                Material m;
                if (y == top) m = y > h * d("snow-line") ? Material.SNOW_BLOCK : Material.GRASS_BLOCK;
                else if (y > top - 3) m = y > h * d("snow-line") ? Material.SNOW_BLOCK : Material.DIRT;
                else m = rnd.nextInt(10) == 0 ? Material.ANDESITE : Material.STONE;
                Blocks.set(b, m);
            }
            return top;
        }), 3000));
        s.sound(s.at(), "block.rooted_dirt.place", 10f, 0.3f);
    }
}
