package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/** A support strike: every player in range (you included) turns invisible and fast. */
public final class CloakField extends Cannon {
    public CloakField() {
        super("&3&lCloak Field", Material.GLASS_PANE, "Support: every player in range, you", "included, turns invisible and fast.");
        warning(5, "#60A0C0");
        noBlockDamage();
        option("radius", 15, "Range.");
        option("seconds", 45, "How long the cloak lasts.");
    }

    @Override
    public void strike(Strike s) {
        int ticks = i("seconds") * 20;
        for (Player p : s.world.getPlayers()) {
            if (p.getLocation().distance(s.at()) > d("radius")) continue;
            p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, ticks, 0));
            p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, ticks, 1));
            Fx.p(s.world, Particle.WITCH, p.getLocation().add(0, 1, 0), 20, 0.4, 0.05);
            s.actionBar(p, "&3You are cloaked.");
        }
        s.sound(s.at(), "entity.illusioner.mirror_move", 6f, 1f);
    }
}
