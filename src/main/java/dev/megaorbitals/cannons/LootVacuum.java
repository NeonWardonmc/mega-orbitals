package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;

/** Sucks up every dropped item and XP orb in a big radius and delivers it all straight to you. */
public final class LootVacuum extends Cannon {
    public LootVacuum() {
        super("&a&lLoot Vacuum", Material.HOPPER, "Sucks up every dropped item and XP orb in a", "big radius and delivers it all to you.");
        warning(10, "#60FF60");
        noBlockDamage();
        option("radius", 64, "Range.");
    }

    @Override
    public void strike(Strike s) {
        Player me = s.shooter;
        if (me == null) return;
        double r = d("radius");
        int items = 0, xp = 0;
        for (Entity e : s.world.getNearbyEntities(s.at(), r, r, r)) {
            if (e instanceof Item it) {
                Fx.line(s.world, Particle.HAPPY_VILLAGER, it.getLocation(), s.at(0, 3, 0), 1.5);
                me.getInventory().addItem(it.getItemStack()).values().forEach(left -> me.getWorld().dropItem(me.getLocation(), left));
                items += it.getItemStack().getAmount();
                it.remove();
            } else if (e instanceof ExperienceOrb orb) {
                me.giveExp(orb.getExperience());
                xp += orb.getExperience();
                orb.remove();
            }
        }
        s.actionBar(me, "&aVacuumed &f" + items + " &aitems and &f" + xp + " &aXP.");
        me.playSound(me.getLocation(), "entity.item.pickup", 1f, 0.6f);
    }
}
