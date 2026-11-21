package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import java.util.*;

/** The two players nearest the target swap bodies: positions, health, hunger and entire inventories. */
public final class BodySwap extends Cannon {
    public BodySwap() {
        super("&d&lBody Swap", Material.ARMOR_STAND, "The two players nearest the target swap bodies:", "positions, health, hunger and inventories.");
        warning(15, "#E060E0");
        noBlockDamage();
        option("range", 30, "How far from the target to find the two players.");
    }

    @Override
    public void strike(Strike s) {
        List<Player> ps = new ArrayList<>(s.world.getPlayers());
        ps.removeIf(p -> p.getLocation().distance(s.at()) > d("range") || s.spared(p) && !p.equals(s.shooter));
        ps.sort(Comparator.comparingDouble(p -> p.getLocation().distanceSquared(s.at())));
        if (ps.size() < 2) {
            if (s.shooter != null) s.actionBar(s.shooter, "&cNeed two players near the target.");
            return;
        }
        Player a = ps.get(0), b = ps.get(1);
        Location la = a.getLocation(), lb = b.getLocation();
        ItemStack[] ia = a.getInventory().getContents(), ib = b.getInventory().getContents();
        double ha = a.getHealth(), hb = b.getHealth();
        int fa = a.getFoodLevel(), fb = b.getFoodLevel();
        a.getInventory().setContents(ib);
        b.getInventory().setContents(ia);
        a.setHealth(Math.min(hb, a.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue()));
        b.setHealth(Math.min(ha, b.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue()));
        a.setFoodLevel(fb);
        b.setFoodLevel(fa);
        a.teleport(lb);
        b.teleport(la);
        Fx.title(a, "&d&lBODY SWAP", "&7You are now " + b.getName(), 40);
        Fx.title(b, "&d&lBODY SWAP", "&7You are now " + a.getName(), 40);
        s.sound(s.at(), "entity.illusioner.mirror_move", 8f, 1f);
    }
}
