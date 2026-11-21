package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/** Every hostile mob for a huge distance drops what it's doing and hunts the player nearest the target. */
public final class Aggro extends Cannon {
    public Aggro() {
        super("&4&lAggro Beacon", Material.ZOMBIE_HEAD, "Every hostile mob for miles hunts down", "the player nearest the target. Enraged.");
        warning(10, "#A02020");
        noBlockDamage();
        option("mob-radius", 96, "How far away mobs are called from.");
        option("target-radius", 30, "How far from the target to find the victim.");
    }

    @Override
    public void strike(Strike s) {
        Player victim = s.nearestPlayer(s.at(), d("target-radius"));
        if (victim == null) {
            if (s.shooter != null) s.actionBar(s.shooter, "&cNo player near the target.");
            return;
        }
        int count = 0;
        double r = d("mob-radius");
        for (Entity e : s.world.getNearbyEntities(s.at(), r, r / 2, r)) {
            if (!(e instanceof Monster m)) continue;
            m.setTarget(victim);
            m.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 600, 1));
            m.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 600, 0));
            count++;
        }
        Fx.title(victim, "&4&lHUNTED", "&7" + count + " monsters are coming for you.", 40);
        s.sound(s.at(), "entity.zombie.ambient", 8f, 0.5f);
    }
}
