package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;

/** Knocks the weapons out of everyone's hands and the armour off their backs, scattering it. */
public final class Disarm extends Cannon {
    public Disarm() {
        super("&6&lDisarm", Material.IRON_SWORD, "Knocks the weapon out of everyone's hands", "and the armour off their backs.");
        warning(20, "#FFB040");
        noBlockDamage();
        option("radius", 20, "Range.");
        option("strip-armor", true, "Also knock off armour.");
        option("scatter", 0.6, "How far the items fly.");
    }

    @Override
    public void strike(Strike s) {
        for (Player p : s.players(s.at(), d("radius"))) {
            PlayerInventory inv = p.getInventory();
            fling(s, p, inv.getItemInMainHand());
            inv.setItemInMainHand(null);
            fling(s, p, inv.getItemInOffHand());
            inv.setItemInOffHand(null);
            if (b("strip-armor")) {
                for (ItemStack a : inv.getArmorContents()) fling(s, p, a);
                inv.setArmorContents(new ItemStack[4]);
            }
            Fx.title(p, "&6&lDISARMED", "", 30);
            s.sound(p.getLocation(), "entity.item.break", 2f, 0.6f);
        }
    }

    private void fling(Strike s, Player p, ItemStack it) {
        if (it == null || it.getType().isAir()) return;
        Item drop = s.world.dropItem(p.getLocation().add(0, 1.2, 0), it.clone());
        double sc = d("scatter");
        drop.setVelocity(new Vector((Math.random() - 0.5) * sc * 2, 0.5, (Math.random() - 0.5) * sc * 2));
        drop.setPickupDelay(40);
    }
}
