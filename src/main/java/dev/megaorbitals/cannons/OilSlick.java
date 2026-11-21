package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/** Coats the ground in slick black oil that slows everyone down... then sets it all alight. */
public final class OilSlick extends Cannon {
    public OilSlick() {
        super("&8&lOil Slick", Material.BLACK_DYE, "Coats the ground in slick oil that slows", "everyone down... then sets it all alight.");
        warning(20, "#202020");
        option("radius", 16, "Size of the slick.");
        option("ignite-seconds", 8, "Seconds before the oil ignites.");
        option("blasts", 8, "Fuel explosions when it ignites.");
    }

    @Override
    public void strike(Strike s) {
        int r = i("radius");
        s.blocks(Blocks.surface(s.at(), r, Material.COAL_BLOCK));
        int ign = i("ignite-seconds") * 20;
        s.every(10, ign / 10, t -> {
            for (LivingEntity le : s.victims(s.at(), r)) le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 15, 2, false, false));
            Fx.block(s.world, Particle.FALLING_DUST, s.randomPoint(r).add(0, 1, 0), Material.BLACK_CONCRETE, 10, 1);
        });
        s.later(ign, () -> {
            s.sound(s.at(), "item.flintandsteel.use", 10f, 0.5f);
            World w = s.world;
            s.blocks(Blocks.columns(s.at(), r, (x, z, dist) -> {
                Block top = s.surface(new Location(w, x + 0.5, s.at().getY(), z + 0.5)).getBlock();
                if (top.getType().isAir() && Math.random() < 0.6) Blocks.put(top, Material.FIRE, true);
                return 3;
            }));
            for (LivingEntity le : s.victims(s.at(), r)) le.setFireTicks(200);
            for (int k = 0; k < i("blasts"); k++) {
                int kk = k;
                s.later(kk * 4L, () -> s.explode(s.randomPoint(r), 2.5f, true));
            }
        });
    }
}
