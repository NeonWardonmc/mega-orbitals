package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import java.util.*;

/** A capture net scoops up every mob in range and gives you their spawn eggs. */
public final class CaptureNet extends Cannon {
    public CaptureNet() {
        super("&a&lCapture Net", Material.COBWEB, "A capture net scoops up every mob in range", "and gives you their spawn eggs.");
        warning(10, "#80C080");
        noBlockDamage();
        option("radius", 15, "Range.");
        option("include-bosses", false, "Also capture withers and wardens.");
    }

    @Override
    public void strike(Strike s) {
        Player me = s.shooter;
        if (me == null) return;
        Map<Material, Integer> eggs = new HashMap<>();
        for (LivingEntity le : s.victims(s.at(), d("radius"))) {
            if (le instanceof Player || le instanceof ArmorStand) continue;
            if (!b("include-bosses") && (le instanceof Wither || le instanceof Warden || le instanceof EnderDragon)) continue;
            Material egg = Material.matchMaterial(le.getType().name() + "_SPAWN_EGG");
            if (egg == null) continue;
            eggs.merge(egg, 1, Integer::sum);
            Fx.p(s.world, Particle.CLOUD, le.getLocation().add(0, 1, 0), 10, 0.3, 0.05);
            le.remove();
        }
        int total = 0;
        for (var e : eggs.entrySet()) {
            me.getInventory().addItem(new ItemStack(e.getKey(), e.getValue())).values().forEach(l -> me.getWorld().dropItem(me.getLocation(), l));
            total += e.getValue();
        }
        s.actionBar(me, "&aCaptured &f" + total + " &amobs.");
        s.sound(s.at(), "entity.item.pickup", 6f, 0.6f);
    }
}
