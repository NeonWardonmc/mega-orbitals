package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import java.util.*;

/** A harmless troll: it rains rotten flesh, poisonous potatoes and spider eyes. Then it all vanishes. */
public final class RottenRain extends Cannon {
    public RottenRain() {
        super("&2&lRotten Rain", Material.ROTTEN_FLESH, "A harmless troll: it rains rotten flesh,", "poisonous potatoes and spider eyes.");
        warning(10, "#608030");
        noBlockDamage();
        option("radius", 15, "Area covered.");
        option("items", 300, "Items dropped.");
        option("vanish-seconds", 30, "Items disappear after this long.");
    }

    @Override
    public void strike(Strike s) {
        Random rnd = new Random();
        Material[] junk = {Material.ROTTEN_FLESH, Material.POISONOUS_POTATO, Material.SPIDER_EYE, Material.DEAD_BUSH, Material.BONE};
        s.every(1, i("items") / 5, t -> {
            for (int k = 0; k < 5; k++) {
                Item it = s.world.dropItem(s.at(rnd.nextGaussian() * d("radius") / 2, 20, rnd.nextGaussian() * d("radius") / 2),
                        new ItemStack(junk[rnd.nextInt(junk.length)]));
                it.setPickupDelay(20);
                s.expire(it, i("vanish-seconds") * 20L);
            }
            if (t % 10 == 0) s.sound(s.at(), "entity.zombie.ambient", 4f, 1.5f);
        });
    }
}
