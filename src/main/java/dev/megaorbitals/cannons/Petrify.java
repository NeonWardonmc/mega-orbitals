package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/** Turns everything in a sphere to plain stone: houses, trees, chests' surroundings, the lot. */
public final class Petrify extends Cannon {
    public Petrify() {
        super("&7&lPetrify", Material.STONE, "Turns everything in a sphere to plain stone:", "houses, trees, the lot.");
        warning(25, "#909090");
        option("radius", 12, "Sphere radius.");
        option("material", "STONE", "What everything turns into.");
        option("skip-containers", true, "Leave chests, barrels and shulker boxes alone.");
    }

    @Override
    public void strike(Strike s) {
        int r = i("radius");
        double r2 = r * r;
        Material to = mat("material");
        boolean skip = b("skip-containers");
        s.blocks(Blocks.box(s.at(), r, r, r, (b, dx, dy, dz) -> {
            if (dx * dx + dy * dy + dz * dz > r2 || b.getType().isAir() || b.isLiquid()) return;
            String n = b.getType().name();
            if (skip && (n.contains("CHEST") || n.contains("BARREL") || n.contains("SHULKER_BOX"))) return;
            Blocks.set(b, to);
        }));
        for (LivingEntity le : s.victims(s.at(), r)) le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 4));
        s.sound(s.at(), "block.stone.place", 10f, 0.4f);
    }
}
