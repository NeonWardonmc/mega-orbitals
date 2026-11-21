package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.util.Vector;

/** Builds a bridge with railings from where you stand straight to the target. */
public final class Bridge extends Cannon {
    public Bridge() {
        super("&7&lInstant Bridge", Material.STONE_BRICK_SLAB, "Builds a bridge with railings from", "where you stand straight to the target.");
        warning(5, "#A0A0A0");
        option("width", 3, "Bridge width.");
        option("deck", "STONE_BRICKS", "Deck material.");
        option("railing", "STONE_BRICK_WALL", "Railing material.");
    }

    @Override
    public void strike(Strike s) {
        if (s.shooter == null || s.shooter.getWorld() != s.world) return;
        Location from = s.shooter.getLocation().subtract(0, 1, 0);
        Location to = s.at().subtract(0, 1, 0);
        Vector path = to.toVector().subtract(from.toVector());
        double len = path.length();
        if (len < 1) return;
        Vector step = path.clone().normalize();
        Vector side = new Vector(-step.getZ(), 0, step.getX()).normalize();
        int half = i("width") / 2;
        Material deck = mat("deck"), rail = mat("railing");
        s.every(1, (int) len + 1, t -> {
            Location c = from.clone().add(step.clone().multiply(t));
            for (int k = -half - 1; k <= half + 1; k++) {
                Location p = c.clone().add(side.clone().multiply(k));
                if (Math.abs(k) <= half) Blocks.set(p.getBlock(), deck);
                else { Blocks.set(p.getBlock(), deck); Blocks.set(p.clone().add(0, 1, 0).getBlock(), rail); }
                for (int y = 1; y <= 3 && Math.abs(k) <= half; y++) Blocks.set(p.clone().add(0, y, 0).getBlock(), Material.AIR);
            }
            if (t % 4 == 0) s.sound(c, "block.stone.place", 2f, 1f);
        });
    }
}
