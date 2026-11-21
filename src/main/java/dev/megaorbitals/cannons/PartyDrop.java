package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import java.util.*;

/** A party crashes in: cakes and candles appear, confetti flies, music plays. */
public final class PartyDrop extends Cannon {
    public PartyDrop() {
        super("&d&lParty Drop", Material.CAKE, "A party crashes in: cakes and candles appear,", "confetti flies and music plays.");
        warning(5, "#FF80C0");
        option("cakes", 8, "Number of cakes.");
        option("radius", 6.0, "Spread.");
    }

    @Override
    public void strike(Strike s) {
        Random rnd = new Random();
        Material[] candles = {Material.RED_CANDLE, Material.YELLOW_CANDLE, Material.LIME_CANDLE, Material.LIGHT_BLUE_CANDLE, Material.PINK_CANDLE};
        if (s.breaks()) {
            for (Location l : Fx.circle(s.at(), d("radius"), i("cakes"))) {
                Block b = s.surface(l).getBlock();
                if (b.getType().isAir()) Blocks.put(b, Material.CAKE, false);
                Block c = s.surface(l.clone().add(rnd.nextGaussian(), 0, rnd.nextGaussian())).getBlock();
                if (c.getType().isAir()) Blocks.put(c, candles[rnd.nextInt(candles.length)], false);
            }
        }
        s.every(4, 40, t -> {
            for (int k = 0; k < 5; k++) {
                Color col = Color.fromRGB(rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
                Fx.dust(s.world, s.at(rnd.nextGaussian() * 4, 4 + rnd.nextDouble() * 3, rnd.nextGaussian() * 4), col, 1.5f, 3, 0.3);
            }
            Fx.p(s.world, Particle.NOTE, s.at(0, 2, 0), 2, 2, 1);
        });
        s.sound(s.at(), "music_disc.cat", 3f, 1f);
        s.sound(s.at(), "entity.player.levelup", 6f, 1.5f);
    }
}
