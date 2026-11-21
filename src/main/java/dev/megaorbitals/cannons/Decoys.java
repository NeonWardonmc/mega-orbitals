package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/** Drops a crowd of decoys that look like you, and turns the real you invisible. */
public final class Decoys extends Cannon {
    public Decoys() {
        super("&5&lSmoke & Mirrors", Material.ARMOR_STAND, "Drops a crowd of decoys that look like you,", "and turns the real you invisible.");
        warning(5, "#8060A0");
        noBlockDamage();
        option("decoys", 8, "Number of decoys.");
        option("radius", 8.0, "Spread.");
        option("seconds", 30, "How long decoys and invisibility last.");
    }

    @Override
    public void strike(Strike s) {
        Player me = s.shooter;
        int ticks = i("seconds") * 20;
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        if (me != null && head.getItemMeta() instanceof SkullMeta sm) { sm.setOwningPlayer(me); head.setItemMeta(sm); }
        for (Location l : Fx.circle(s.at(), d("radius"), i("decoys"))) {
            Location g = s.surface(l);
            if (me != null) g.setDirection(me.getLocation().getDirection());
            ArmorStand a = s.world.spawn(g, ArmorStand.class, st -> {
                st.setArms(true);
                st.setBasePlate(false);
                st.getEquipment().setHelmet(head.clone());
                if (me != null) {
                    st.customName(s.text("&f" + me.getName()));
                    st.setCustomNameVisible(true);
                    ItemStack[] armor = me.getInventory().getArmorContents();
                    if (armor[2] != null) st.getEquipment().setChestplate(armor[2].clone());
                    if (armor[1] != null) st.getEquipment().setLeggings(armor[1].clone());
                    if (armor[0] != null) st.getEquipment().setBoots(armor[0].clone());
                    st.getEquipment().setItemInMainHand(me.getInventory().getItemInMainHand().clone());
                }
            });
            s.expire(s.tag(a), ticks);
            Fx.p(s.world, Particle.CLOUD, g, 10, 0.3, 0.05);
        }
        if (me != null) me.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, ticks, 0));
        s.sound(s.at(), "entity.illusioner.cast_spell", 6f, 1f);
    }
}
