package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/** Builds a random spiral parkour course into the sky with a prize chest waiting at the top. */
public final class ParkourTower extends Cannon {

    private static final Material[] STEPS = {Material.RED_CONCRETE, Material.ORANGE_CONCRETE, Material.YELLOW_CONCRETE,
            Material.LIME_CONCRETE, Material.LIGHT_BLUE_CONCRETE, Material.BLUE_CONCRETE, Material.PURPLE_CONCRETE, Material.MAGENTA_CONCRETE};

    public ParkourTower() {
        super("&e&lParkour Tower", Material.LADDER, "Builds a random climbing course into the sky", "with a prize chest at the top.");
        warning(20, "#FFE040");
        option("jumps", 24, "Number of jumps in the course.");
        option("radius", 4.5, "Radius of the spiral.");
        option("prize", List.of("DIAMOND:5", "GOLDEN_APPLE:3", "EMERALD:8", "EXPERIENCE_BOTTLE:16"), "What is in the chest at the top, MATERIAL:amount.");
    }

    @Override
    public void strike(Strike s) {
        if (!s.breaks()) { s.tell("&cParkour Tower needs break-blocks: true."); return; }
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        Location base = s.at();
        double r = Math.max(3, d("radius"));
        int jumps = Math.max(3, Math.min(120, i("jumps")));
        double ang = rnd.nextDouble(Math.PI * 2);
        int dirSign = rnd.nextBoolean() ? 1 : -1;
        List<Block> path = new ArrayList<>();
        Block start = base.getBlock().getRelative(0, -1, 0);
        Blocks.set(start, Material.GOLD_BLOCK);
        Block prev = start;
        for (int k = 0; k < jumps; k++) {
            Block next = null;
            for (int attempt = 0; attempt < 10 && next == null; attempt++) {
                double stepAng = (2.6 + rnd.nextDouble() * 0.6) / r;
                double a = ang + dirSign * stepAng;
                double rr = r + rnd.nextDouble(-0.8, 0.8);
                int x = (int) Math.round(base.getX() - 0.5 + Math.cos(a) * rr), z = (int) Math.round(base.getZ() - 0.5 + Math.sin(a) * rr);
                Block cand = s.world.getBlockAt(x, prev.getY() + 1, z);
                int dx = cand.getX() - prev.getX(), dz = cand.getZ() - prev.getZ();
                int gap = Math.max(Math.abs(dx), Math.abs(dz));
                if (gap < 2 || gap > 3 || dx * dx + dz * dz > 10) continue;
                if (cand.getY() >= s.world.getMaxHeight() - 4) break;
                ang = a;
                next = cand;
            }
            if (next == null) break;
            Blocks.set(next, STEPS[k % STEPS.length]);
            for (int y = 1; y <= 2; y++) {
                Block above = next.getRelative(0, y, 0);
                if (!above.getType().isAir()) Blocks.set(above, Material.AIR);
            }
            path.add(next);
            prev = next;
        }
        // prize platform
        for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++) {
            Block b = prev.getRelative(x, 0, z);
            if (b.getType().isAir()) Blocks.set(b, Material.QUARTZ_BLOCK);
            for (int y = 1; y <= 2; y++) {
                Block above = b.getRelative(0, y, 0);
                if (!above.getType().isAir() && !(x == 0 && z == 0 && y == 1)) Blocks.set(above, Material.AIR);
            }
        }
        Block chest = prev.getRelative(0, 1, 0);
        Blocks.set(chest, Material.CHEST);
        if (chest.getState() instanceof Container box) {
            List<ItemStack> prize = Strike.items(list("prize"));
            for (ItemStack it : prize) box.getInventory().addItem(it);
        }
        Location topLoc = chest.getLocation().add(0.5, 1.2, 0.5);
        s.every(20, 120, t -> {
            Fx.p(s.world, Particle.HAPPY_VILLAGER, topLoc, 6, 0.4, 0);
            Fx.p(s.world, Particle.END_ROD, base.clone().add(0, 1, 0), 3, 0.3, 0.02);
        });
        s.sound(base, "block.note_block.chime", 4f, 1f);
        for (Player p : s.world.getPlayers()) if (p.getLocation().distance(base) < 40)
            Fx.title(p, "&e&lPARKOUR TOWER", "&7" + path.size() + " jumps to the prize!", 50);
    }
}
