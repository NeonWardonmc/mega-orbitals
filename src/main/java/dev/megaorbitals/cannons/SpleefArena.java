package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;

/** Minigame: everyone nearby is dropped onto a snow platform high in the sky with a shovel. Last one up wins. */
public final class SpleefArena extends Cannon {
    public SpleefArena() {
        super("&f&lSky Spleef", Material.DIAMOND_SHOVEL, "Minigame: everyone nearby lands on a snow platform", "high in the sky, with a shovel. Last one up wins.");
        warning(20, "#F0F0F0");
        option("radius", 20, "Who gets pulled in.");
        option("platform-radius", 10, "Platform size.");
        option("height", 60, "Platform height above the target.");
    }

    @Override
    public void strike(Strike s) {
        if (!s.breaks()) return;
        int pr = i("platform-radius");
        Location c = s.at(0, i("height"), 0);
        for (int x = -pr; x <= pr; x++) for (int z = -pr; z <= pr; z++) {
            if (x * x + z * z <= pr * pr) Blocks.set(c.clone().add(x, -1, z).getBlock(), Material.SNOW_BLOCK);
        }
        int k = 0;
        var players = s.players(s.at(), d("radius"));
        for (Player p : players) {
            double a = 2 * Math.PI * k++ / Math.max(1, players.size());
            p.teleport(c.clone().add(Math.cos(a) * (pr - 2), 0, Math.sin(a) * (pr - 2)));
            ItemStack shovel = new ItemStack(Material.DIAMOND_SHOVEL);
            shovel.addUnsafeEnchantment(Enchantment.EFFICIENCY, 5);
            p.getInventory().addItem(shovel);
            Fx.title(p, "&f&lSPLEEF!", "&7Dig out the floor under everyone else.", 50);
        }
        s.sound(c, "block.snow.place", 10f, 0.8f);
    }
}
