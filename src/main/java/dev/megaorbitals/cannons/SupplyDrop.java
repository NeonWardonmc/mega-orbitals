package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import java.util.*;

/** Support: a supply crate parachutes down from orbit, packed with loot. Edit the loot in the config. */
public final class SupplyDrop extends Cannon {
    public SupplyDrop() {
        super("&6&lSupply Drop", Material.CHEST, "Support: a supply crate parachutes down", "from orbit, packed with loot.");
        warning(10, "#E0A040");
        option("loot", List.of("GOLDEN_APPLE:4", "DIAMOND:6", "IRON_INGOT:16", "ENDER_PEARL:4", "ARROW:32",
                "COOKED_BEEF:16", "EXPERIENCE_BOTTLE:8", "TOTEM_OF_UNDYING:1"), "Items in the crate, MATERIAL:amount.");
        option("fall-seconds", 6, "How long the crate takes to float down.");
    }

    @Override
    public void strike(Strike s) {
        Location top = s.at(0, 60, 0);
        BlockDisplay crate = Fx.display(top, Material.CHEST, 1.2f);
        BlockDisplay chute = Fx.display(top.clone().add(0, 3, 0), Material.WHITE_WOOL, 4f, 0.4f, 4f);
        int ticks = i("fall-seconds") * 20;
        s.every(1, ticks + 1, t -> {
            Location l = top.clone().subtract(0, 60.0 * t / ticks, 0);
            crate.teleport(l);
            chute.teleport(l.clone().add(0, 3, 0));
            if (t % 4 == 0) Fx.p(s.world, Particle.CAMPFIRE_SIGNAL_SMOKE, s.at(), 1, 0.1, 0.05);
            if (t < ticks) return;
            crate.remove();
            chute.remove();
            Block b = s.at().getBlock();
            b.setType(Material.CHEST);
            if (b.getState() instanceof Chest chest) {
                for (String entry : list("loot")) {
                    String[] parts = entry.split(":");
                    Material m = Material.matchMaterial(parts[0].trim());
                    if (m == null) continue;
                    chest.getBlockInventory().addItem(new ItemStack(m, parts.length > 1 ? Integer.parseInt(parts[1].trim()) : 1));
                }
            }
            s.sound(s.at(), "block.chest.open", 4f, 0.8f);
            Fx.p(s.world, Particle.FIREWORK, s.at(0, 1, 0), 40, 0.5, 0.2);
        });
    }
}
