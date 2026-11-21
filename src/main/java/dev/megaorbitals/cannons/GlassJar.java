package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/** Slams a giant tinted glass dome over the area and weakens everyone inside so they can't dig out. */
public final class GlassJar extends Cannon {
    public GlassJar() {
        super("&7&lGlass Jar", Material.TINTED_GLASS, "Traps the area under a giant dark glass dome.", "Everyone inside is too weak to dig out.");
        warning(30, "#808080");
        option("radius", 20, "Dome radius.");
        option("weak-seconds", 60, "How long everyone inside has mining fatigue.");
        option("material", "TINTED_GLASS", "What the dome is made of.");
    }

    @Override
    public void strike(Strike s) {
        int r = i("radius");
        Material m = mat("material");
        s.blocks(Blocks.box(s.at(0, -1, 0), r, r, r, (b, dx, dy, dz) -> {
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (dy >= 0 && dist <= r && dist > r - 1.2 && (b.getType().isAir() || !b.getType().isSolid())) Blocks.set(b, m);
        }));
        for (LivingEntity le : s.victims(s.at(), r)) {
            le.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, i("weak-seconds") * 20, 3));
            le.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, i("weak-seconds") * 20, 2));
            if (le instanceof Player p) Fx.title(p, "&7&lTRAPPED", "&8You're in the jar now.", 40);
        }
        s.sound(s.at(), "block.glass.place", 10f, 0.5f);
    }
}
