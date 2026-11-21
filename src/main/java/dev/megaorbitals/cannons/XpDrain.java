package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;

/** Steals experience levels from every player nearby and gives them to you. */
public final class XpDrain extends Cannon {
    public XpDrain() {
        super("&a&lXP Heist", Material.EXPERIENCE_BOTTLE, "Steals experience levels from everyone", "nearby and gives them to you.");
        warning(20, "#80FF40");
        noBlockDamage();
        option("radius", 30, "Range.");
        option("fraction", 1.0, "Fraction of their levels to steal (1.0 = all).");
    }

    @Override
    public void strike(Strike s) {
        int total = 0;
        for (Player p : s.players(s.at(), d("radius"))) {
            int take = (int) Math.round(p.getLevel() * Math.max(0, Math.min(1, d("fraction"))));
            if (take <= 0) continue;
            p.setLevel(p.getLevel() - take);
            total += take;
            Fx.title(p, "&a&lROBBED", "&7You lost " + take + " levels.", 30);
            Location to = s.shooter != null && s.shooter.getWorld() == s.world ? s.shooter.getLocation().add(0, 1, 0) : s.at(0, 10, 0);
            Fx.line(s.world, Particle.HAPPY_VILLAGER, p.getLocation().add(0, 1, 0), to, 0.5);
        }
        if (s.shooter != null && total > 0) {
            s.shooter.giveExpLevels(total);
            s.actionBar(s.shooter, "&aStole &f" + total + " &alevels.");
            s.shooter.playSound(s.shooter.getLocation(), "entity.player.levelup", 1f, 0.8f);
        }
    }
}
