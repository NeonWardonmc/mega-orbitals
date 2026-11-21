package dev.megaorbitals.cannons;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import java.util.*;

/**
 * Femboyify: every player nearby is given the same skin (namemc.com/skin/ace8ad0f7f6674d3, slim model),
 * and nearby mobs get a head with that skin. Players change back when the timer ends or when they rejoin.
 * The skin is a Mojang-signed texture; swap texture-value / texture-signature in the config for any other skin.
 */
public final class Femboyify extends Cannon {

    private static final String DEFAULT_VALUE = "ewogICJ0aW1lc3RhbXAiIDogMTc5NTI0NDI1MzAyMiwKICAicHJvZmlsZUlkIiA6ICI4NTQyNjc1ZjExNDk0OTYzYWM4MWE3OGRmNjNkZTdlNiIsCiAgInByb2ZpbGVOYW1lIiA6ICJpaWlWb2RvbyIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9jMjc2NTEyNmYzZmVmZDhhODQ3NmU3MGE4NWJmYWI1NjI3MzI0YjA0MmVlOGJjMzgxNDMzNThhNjA3Njc2ZDk5IiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=";
    private static final String DEFAULT_SIGNATURE = "TxOzTHtKpKWKiLrda8eQSeFF3VDeVqy0CfuQom5z2AqM0QlODm0Cb3g6X0Un2GTn7JTtvn0TSgFExetKx05KDl2SvzEld86nU32Dgwq77zueiODsXRmmuv2RTYHXz+4Zdnx0siKeJS1N2gDAbwJZGEW0RvG3YzQcu3uXAKtdGMMbmivtP+2tdxKfdebvOKYcqLR0WEm4D/F5e1LHUBzWq4aU7ok7j4loIwWsjrHDYiZLKO1ik2km+z7JRRWyV0apGqiJw1fFr50Klc7TEvCiM67b9uS1qG10Z7PytUudUJLFOd0CR5nhvWxXwfoYNXqeFM+O7Z01wUuK1944DjUovr9r9eGGODbni/Q214Ee34B6gK9TfpfO1uuQyA4vvdmb7zrNBSQF7GFHJNId7rzQfIdlrbIcuR003wyumb/8q0f20F4IHGcWnL64IgKI07beZ/7GCkDYcamiRYDxtlQWBkuol/XFuc55O+zdW9in0rig0+iNGUR/zG1RRKDlgPJHZR9cpSDj4TazwsJQ0/XkhaV9d851Jr+S9p7bEbTcssT0uBYWehc3W2DibjrXix5xEaIfkTWQtPBf1kec7+JKjeLHcAJAFRzCHMN+S6ANK+a9hhBzB5wKcrLIsjWlwwyw+PdJsZcWTregnBE7w+ofMMbgr/wmMG0Ft4xCMTyKUWk=";

    /** Original skins of players currently changed, so they can be put back. */
    private final Map<UUID, ProfileProperty> originals = new HashMap<>();

    public Femboyify() {
        super("&d&lFemboyify", Material.PINK_TULIP, "Everyone nearby gets the same cute skin.", "Mobs get the matching head too.");
        warning(20, "#FF80D0");
        noBlockDamage();
        option("radius", 25, "Range.");
        option("seconds", 300, "How long the skin lasts (0 = until they rejoin).");
        option("include-mobs", true, "Put the skin's head on nearby mobs too.");
        option("texture-value", DEFAULT_VALUE, "Signed skin texture value (from Mojang / mineskin.org). Change to use another skin.");
        option("texture-signature", DEFAULT_SIGNATURE, "Signature for the texture value above.");
    }

    private ProfileProperty skin() {
        String sig = str("texture-signature");
        return new ProfileProperty("textures", str("texture-value"), sig.isEmpty() ? null : sig);
    }

    @Override
    public void strike(Strike s) {
        ProfileProperty skin = skin();
        Color pink = Color.fromRGB(255, 130, 210);
        int changed = 0;
        for (Player p : s.players(s.at(), d("radius"))) {
            apply(p, skin);
            changed++;
            Fx.p(s.world, Particle.HEART, p.getLocation().add(0, 2, 0), 8, 0.4, 0);
            Fx.dust(s.world, p.getLocation().add(0, 1, 0), pink, 1.5f, 30, 0.5);
            Fx.title(p, "&d&l♡ FEMBOYIFIED ♡", i("seconds") > 0 ? "&7for " + i("seconds") + " seconds" : "&7until you rejoin", 40);
            if (i("seconds") > 0) s.later(i("seconds") * 20L, () -> restore(p));
        }
        if (b("include-mobs")) {
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            if (head.getItemMeta() instanceof SkullMeta meta) {
                PlayerProfile prof = Bukkit.createProfile(UUID.randomUUID(), "Femboy");
                prof.setProperty(skin);
                meta.setPlayerProfile(prof);
                head.setItemMeta(meta);
            }
            for (LivingEntity le : s.victims(s.at(), d("radius"))) {
                if (le instanceof Player || !(le instanceof Mob m) || m.getEquipment() == null) continue;
                m.getEquipment().setHelmet(head.clone());
                m.getEquipment().setHelmetDropChance(0);
                Fx.p(s.world, Particle.HEART, le.getLocation().add(0, le.getHeight() + 0.3, 0), 3, 0.3, 0);
            }
        }
        Fx.dustRing(s.world, s.at(0, 0.3, 0), d("radius"), pink, 2f);
        s.sound(s.at(), "entity.allay.ambient_with_item", 8f, 1.2f);
        if (s.shooter != null) s.actionBar(s.shooter, "&dFemboyified &f" + changed + " &dplayers ♡");
    }

    private void apply(Player p, ProfileProperty skin) {
        PlayerProfile prof = p.getPlayerProfile();
        if (!originals.containsKey(p.getUniqueId())) {
            ProfileProperty old = null;
            for (ProfileProperty pp : prof.getProperties()) if (pp.getName().equals("textures")) old = pp;
            originals.put(p.getUniqueId(), old);
        }
        prof.setProperty(skin);
        p.setPlayerProfile(prof);
    }

    private void restore(Player p) {
        if (!originals.containsKey(p.getUniqueId())) return;
        ProfileProperty old = originals.remove(p.getUniqueId());
        if (!p.isOnline()) return;
        PlayerProfile prof = p.getPlayerProfile();
        prof.removeProperty("textures");
        if (old != null) prof.setProperty(old);
        p.setPlayerProfile(prof);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        originals.remove(e.getPlayer().getUniqueId());
    }
}
