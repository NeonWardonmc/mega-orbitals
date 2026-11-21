package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import java.util.*;

/** A harmless orbital rave: flashing dance floor, music, lasers, and everyone is forced to dance. */
public final class Disco extends Cannon {
    public Disco() {
        super("&d&lOrbital Rave", Material.MUSIC_DISC_PIGSTEP, "A flashing dance floor, music and lasers.", "Everyone nearby is forced to dance.");
        warning(10, "#FF40FF");
        option("radius", 10, "Dance floor radius.");
        option("seconds", 30, "Length of the party.");
        option("music", "music_disc.pigstep", "Sound that plays.");
    }

    private static final Material[] FLOOR = {Material.RED_CONCRETE, Material.ORANGE_CONCRETE, Material.YELLOW_CONCRETE, Material.LIME_CONCRETE,
            Material.LIGHT_BLUE_CONCRETE, Material.BLUE_CONCRETE, Material.PURPLE_CONCRETE, Material.MAGENTA_CONCRETE};
    private static final Color[] LIGHTS = {Color.RED, Color.ORANGE, Color.YELLOW, Color.LIME, Color.AQUA, Color.BLUE, Color.PURPLE, Color.FUCHSIA};

    @Override
    public void strike(Strike s) {
        Random rnd = new Random();
        int r = i("radius");
        Map<Block, BlockData> floor = new HashMap<>();
        if (s.breaks()) {
            for (int x = -r; x <= r; x++) for (int z = -r; z <= r; z++) {
                if (x * x + z * z > r * r) continue;
                Block b = s.surface(s.at(x, 0, z)).getBlock().getRelative(0, -1, 0);
                if (Blocks.breakable(b) && !b.getType().isAir()) floor.put(b, b.getBlockData());
            }
        }
        s.sound(s.at(), str("music"), 4f, 1f);
        int ticks = i("seconds") * 20;
        s.every(5, ticks / 5, t -> {
            for (Block b : floor.keySet()) if (rnd.nextInt(3) == 0) b.setType(FLOOR[rnd.nextInt(FLOOR.length)], false);
            Location top = s.at(0, 12, 0);
            for (int k = 0; k < 6; k++) {
                Location to = s.at(rnd.nextGaussian() * r, 0, rnd.nextGaussian() * r);
                Fx.dustLine(s.world, top, to, LIGHTS[rnd.nextInt(LIGHTS.length)], 1f, 0.6);
            }
            Fx.p(s.world, Particle.NOTE, s.at(0, 2, 0), 5, r / 2.0, 1);
            for (LivingEntity le : s.victims(s.at(), r + 2)) {
                if (le.isOnGround()) le.setVelocity(new Vector(rnd.nextGaussian() * 0.15, 0.42, rnd.nextGaussian() * 0.15));
                le.setRotation(le.getLocation().getYaw() + 45, le.getLocation().getPitch());
                le.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 20, 0, false, false));
            }
        });
        s.later(ticks, () -> floor.forEach((b, data) -> b.setBlockData(data, false)));
    }
}
