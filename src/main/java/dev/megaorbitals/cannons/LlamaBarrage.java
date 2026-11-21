package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import java.util.*;

/** A sky full of llama spit. Disgusting, humiliating, and it really adds up. */
public final class LlamaBarrage extends Cannon {
    public LlamaBarrage() {
        super("&e&lLlama Barrage", Material.WHITE_CARPET, "A sky full of llama spit. Disgusting,", "humiliating, and it really adds up.");
        warning(15, "#F0E0B0");
        noBlockDamage();
        option("radius", 25, "Range.");
        option("seconds", 8, "How long the spitting lasts.");
        option("spit-per-target", 2, "Spit per target per volley.");
        option("damage", 1.0, "Extra damage per spit that lands near you.");
    }

    @Override
    public void strike(Strike s) {
        Random rnd = new Random();
        s.every(4, i("seconds") * 5, t -> {
            for (LivingEntity le : s.victims(s.at(), d("radius"))) {
                for (int k = 0; k < i("spit-per-target"); k++) {
                    Location from = le.getLocation().add(rnd.nextGaussian() * 5, 12, rnd.nextGaussian() * 5);
                    LlamaSpit spit = s.world.spawn(from, LlamaSpit.class, sp -> sp.setVelocity(Fx.dir(from, le.getEyeLocation()).multiply(1.8)));
                    s.expire(s.tag(spit), 60);
                }
                if (t % 2 == 0) s.damage(le, d("damage"));
            }
            s.sound(s.at(), "entity.llama.spit", 6f, 1f);
        });
    }
}
