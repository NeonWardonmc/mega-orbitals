package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

/** A gorgon's head hovers over the target. Anyone who looks at it is turned to stone. */
public final class Medusa extends Cannon {
    public Medusa() {
        super("&2&lMedusa's Gaze", Material.CREEPER_HEAD, "A gorgon's head hovers over the target.", "Anyone who looks at it turns to stone.");
        warning(10, "#30A030");
        noBlockDamage();
        option("range", 50, "How far away its gaze works.");
        option("seconds", 20, "How long the head stays.");
        option("stone-seconds", 5, "How long you stay petrified.");
        option("damage", 3.0, "Damage each time you're petrified.");
    }

    @Override
    public void strike(Strike s) {
        Location head = s.at(0, 6, 0);
        BlockDisplay d = Fx.display(head, Material.MOSSY_COBBLESTONE, 2f);
        s.every(5, i("seconds") * 4, t -> {
            Fx.p(s.world, Particle.HAPPY_VILLAGER, head, 6, 1, 0);
            for (Player p : s.players(head, d("range"))) {
                Vector toHead = Fx.dir(p.getEyeLocation(), head);
                if (p.getEyeLocation().getDirection().dot(toHead) < 0.9) continue;
                if (!p.hasLineOfSight(d)) continue;
                int st = i("stone-seconds") * 20;
                p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, st, 254, false, false));
                p.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, st, 4, false, false));
                p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, st, 250, false, false));
                p.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, st, 0, false, false));
                s.damage(p, d("damage"));
                Fx.block(s.world, Particle.BLOCK, p.getLocation().add(0, 1, 0), Material.STONE, 30, 0.4);
                Fx.title(p, "&7&lPETRIFIED", "&8Don't look at it.", 30);
            }
            if (t % 8 == 0) s.sound(head, "entity.evoker.prepare_wololo", 4f, 0.5f);
            if (t == i("seconds") * 4 - 1) d.remove();
        });
    }
}
