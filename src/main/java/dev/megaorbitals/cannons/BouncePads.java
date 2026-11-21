package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;

/** The ground becomes a giant trampoline that keeps launching everyone sky-high. */
public final class BouncePads extends Cannon {
    public BouncePads() {
        super("&a&lTrampoline", Material.SLIME_BLOCK, "The ground becomes a giant trampoline", "that keeps launching everyone sky-high.");
        warning(20, "#60FF60");
        option("radius", 15, "Size of the trampoline.");
        option("seconds", 20, "How long the launching lasts.");
        option("launch", 2.2, "Launch strength.");
    }

    @Override
    public void strike(Strike s) {
        int r = i("radius");
        s.blocks(Blocks.surface(s.at(), r, Material.SLIME_BLOCK));
        s.every(2, i("seconds") * 10, t -> {
            for (LivingEntity le : s.victims(s.at(), r)) {
                if (!le.isOnGround()) continue;
                le.setVelocity(le.getVelocity().setY(d("launch")));
                le.setFallDistance(0);
                s.sound(le.getLocation(), "block.slime_block.fall", 2f, 0.6f);
            }
        });
    }
}
