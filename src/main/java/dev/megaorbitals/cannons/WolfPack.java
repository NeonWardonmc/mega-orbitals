package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;

/** Drops a pack of armoured wolves that are loyal to you and attack everyone else. */
public final class WolfPack extends Cannon {
    public WolfPack() {
        super("&7&lWolf Pack", Material.BONE, "A pack of armoured wolves loyal to you.", "They attack everyone else nearby.");
        warning(20, "#A0A0A0");
        option("wolves", 8, "Number of wolves.");
        option("hunt-radius", 40, "How far they look for targets.");
    }

    @Override
    public void strike(Strike s) {
        for (Location l : Fx.circle(s.at(), 3, i("wolves"))) {
            Wolf w = s.mob(s.surface(l), Wolf.class, wo -> {
                if (s.shooter != null) { wo.setTamed(true); wo.setOwner(s.shooter); }
                wo.getEquipment().setItem(org.bukkit.inventory.EquipmentSlot.BODY, new ItemStack(Material.WOLF_ARMOR));
            });
            s.hunt(w, d("hunt-radius"));
        }
        s.sound(s.at(), "entity.wolf.howl", 10f, 1f);
    }
}
