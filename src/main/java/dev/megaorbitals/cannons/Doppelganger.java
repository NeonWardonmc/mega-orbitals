package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.PlayerInventory;

/** Every player nearby is hunted by an evil copy of themselves wearing their exact gear. */
public final class Doppelganger extends Cannon {
    public Doppelganger() {
        super("&5&lDoppelganger", Material.ARMOR_STAND, "Every player nearby is hunted by an evil", "copy of themselves, wearing their gear.");
        warning(30, "#602080");
        option("radius", 30, "Range.");
        option("copies-each", 2, "Copies per player.");
        option("health", 40.0, "Health of each copy.");
    }

    @Override
    public void strike(Strike s) {
        for (Player p : s.players(s.at(), d("radius"))) {
            PlayerInventory inv = p.getInventory();
            for (int k = 0; k < i("copies-each"); k++) {
                Location l = s.surface(p.getLocation().add(Math.random() * 6 - 3, 0, Math.random() * 6 - 3));
                Zombie z = s.mob(l, Zombie.class, zz -> {
                    zz.customName(s.text("&5" + p.getName() + "'s Doppelganger"));
                    zz.setCustomNameVisible(true);
                    zz.setShouldBurnInDay(false);
                    zz.setAdult();
                    var hp = zz.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH);
                    if (hp != null) { hp.setBaseValue(d("health")); zz.setHealth(d("health")); }
                    EntityEquipment eq = zz.getEquipment();
                    eq.setHelmet(inv.getHelmet() == null ? new org.bukkit.inventory.ItemStack(Material.PLAYER_HEAD) : inv.getHelmet().clone());
                    if (inv.getChestplate() != null) eq.setChestplate(inv.getChestplate().clone());
                    if (inv.getLeggings() != null) eq.setLeggings(inv.getLeggings().clone());
                    if (inv.getBoots() != null) eq.setBoots(inv.getBoots().clone());
                    eq.setItemInMainHand(inv.getItemInMainHand().clone());
                    eq.setHelmetDropChance(0); eq.setChestplateDropChance(0); eq.setLeggingsDropChance(0);
                    eq.setBootsDropChance(0); eq.setItemInMainHandDropChance(0);
                });
                z.setTarget(p);
                Fx.p(s.world, Particle.WITCH, l, 30, 0.5, 0.05);
            }
            Fx.title(p, "&5&lWHO IS THAT?", "&7Something that looks like you...", 40);
        }
    }
}
