package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import java.util.*;

/** Buries a treasure chest somewhere nearby. Everyone close gets hot/cold hints. First to dig it up wins. */
public final class TreasureHunt extends Cannon {
    public TreasureHunt() {
        super("&6&lTreasure Hunt", Material.FILLED_MAP, "Buries a treasure chest somewhere nearby.", "Everyone close gets hot/cold hints. Dig it up!");
        warning(10, "#E0B040");
        option("radius", 50, "How far away the treasure can be buried.");
        option("hint-radius", 120, "Players this close get hints.");
        option("minutes", 10, "How long hints last.");
        option("loot", List.of("DIAMOND:10", "GOLD_BLOCK:3", "ENCHANTED_GOLDEN_APPLE:1", "NETHERITE_SCRAP:2", "EMERALD:16"), "Treasure, MATERIAL:amount.");
    }

    @Override
    public void strike(Strike s) {
        Location spot = s.randomPoint(d("radius")).subtract(0, 3 + new Random().nextInt(3), 0);
        Block b = spot.getBlock();
        Blocks.put(b, Material.CHEST);
        if (b.getState() instanceof Chest chest) {
            for (String e : list("loot")) {
                String[] p = e.split(":");
                Material m = Material.matchMaterial(p[0].trim());
                if (m != null) chest.getBlockInventory().addItem(new ItemStack(m, p.length > 1 ? Integer.parseInt(p[1].trim()) : 1));
            }
        }
        for (Player p : s.world.getPlayers()) if (p.getLocation().distance(s.at()) <= d("hint-radius")) Fx.title(p, "&6&lTREASURE HUNT", "&7A chest is buried nearby. Follow the hints!", 50);
        s.every(20, i("minutes") * 60, t -> {
            if (b.getType() != Material.CHEST) return;
            for (Player p : s.world.getPlayers()) {
                double dist = p.getLocation().distance(spot);
                if (dist > d("hint-radius")) continue;
                String hint = dist < 4 ? "&4&lBURNING HOT - dig down!" : dist < 10 ? "&c&lHOT" : dist < 20 ? "&6Warm" : dist < 40 ? "&eLukewarm" : "&bCold";
                s.actionBar(p, "&6Treasure: " + hint + " &8(" + (int) dist + "m)");
            }
        });
    }
}
