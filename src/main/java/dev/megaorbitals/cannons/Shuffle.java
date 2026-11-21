package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import java.util.*;

/** Scrambles every nearby player's inventory, so nothing is where they left it. */
public final class Shuffle extends Cannon {
    public Shuffle() {
        super("&d&lInventory Shuffle", Material.BUNDLE, "Scrambles every nearby player's inventory.", "Good luck finding your sword.");
        warning(20, "#FF80FF");
        noBlockDamage();
        option("radius", 25, "Range.");
        option("swap-between-players", false, "Also mix items between different players.");
    }

    @Override
    public void strike(Strike s) {
        List<Player> players = s.players(s.at(), d("radius"));
        if (b("swap-between-players") && players.size() > 1) {
            List<ItemStack> pool = new ArrayList<>();
            for (Player p : players) pool.addAll(Arrays.asList(p.getInventory().getStorageContents()));
            Collections.shuffle(pool);
            int idx = 0;
            for (Player p : players) {
                ItemStack[] c = new ItemStack[p.getInventory().getStorageContents().length];
                for (int k = 0; k < c.length; k++) c[k] = pool.get(idx++);
                p.getInventory().setStorageContents(c);
            }
        } else {
            for (Player p : players) {
                List<ItemStack> items = Arrays.asList(p.getInventory().getStorageContents());
                Collections.shuffle(items);
                p.getInventory().setStorageContents(items.toArray(new ItemStack[0]));
            }
        }
        for (Player p : players) {
            Fx.title(p, "&d&lSHUFFLED", "&7Check your hotbar.", 30);
            p.playSound(p.getLocation(), "item.bundle.drop_contents", 1f, 1f);
        }
    }
}
