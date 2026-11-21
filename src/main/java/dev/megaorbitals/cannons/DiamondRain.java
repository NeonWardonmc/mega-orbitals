package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import java.util.*;

/** Crushing blocks of diamond rain from the sky. They shatter on impact, so nobody gets rich. */
public final class DiamondRain extends Cannon {
    public DiamondRain() {
        super("&b&lDiamond Rain", Material.DIAMOND_BLOCK, "Crushing blocks of diamond fall from the sky.", "They shatter on impact. Nobody gets rich.");
        warning(20, "#60F0FF");
        option("radius", 15, "Area covered.");
        option("blocks", 80, "Number of diamond blocks.");
        option("damage-per-block-fallen", 2.0, "Damage per block of fall height.");
        option("max-damage", 40, "Damage cap per hit.");
    }

    @Override
    public void strike(Strike s) {
        Random rnd = new Random();
        s.every(2, i("blocks") / 4, t -> {
            for (int k = 0; k < 4; k++) {
                Location l = s.at(rnd.nextGaussian() * d("radius") / 2, 30 + rnd.nextInt(10), rnd.nextGaussian() * d("radius") / 2);
                FallingBlock fb = s.fallingBlock(l, Material.DIAMOND_BLOCK, true);
                fb.setHurtEntities(true);
                fb.setDamagePerBlock((float) d("damage-per-block-fallen"));
                fb.setMaxDamage(i("max-damage"));
            }
            if (t % 5 == 0) s.sound(s.at(), "block.amethyst_block.fall", 6f, 0.5f);
        });
    }
}
