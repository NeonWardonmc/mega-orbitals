package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;

/** A resonance pulse that shatters every fragile block in a huge area: glass, ice, leaves, lights. */
public final class Shatter extends Cannon {
    public Shatter() {
        super("&b&lResonance Shatter", Material.GLASS, "Shatters every fragile block in a huge area:", "glass, ice, leaves, lanterns and more.");
        warning(30, "#C0F0FF");
        option("radius", 50, "Range.");
        option("height", 40, "Vertical range above and below.");
    }

    private static boolean fragile(Material m) {
        String n = m.name();
        return n.contains("GLASS") || n.contains("ICE") || n.contains("LEAVES") || n.contains("LANTERN") || n.equals("GLOWSTONE")
                || n.contains("TORCH") || n.equals("SEA_LANTERN") || n.contains("CANDLE") || n.contains("AMETHYST")
                || n.equals("REDSTONE_LAMP") || n.equals("BEACON") || n.contains("FLOWER_POT") || n.equals("SHROOMLIGHT")
                || n.contains("CORAL") || n.equals("FROGLIGHT") || n.endsWith("_FROGLIGHT") || n.equals("END_ROD");
    }

    @Override
    public void strike(Strike s) {
        int r = i("radius");
        int r2 = r * r;
        s.blocks(Blocks.box(s.at(), r, i("height"), r, (b, dx, dy, dz) -> {
            if (dx * dx + dz * dz <= r2 && fragile(b.getType())) {
                Fx.block(s.world, Particle.BLOCK, b.getLocation().add(0.5, 0.5, 0.5), b.getType(), 4, 0.3);
                Blocks.set(b, Material.AIR);
            }
        }));
        s.every(2, 20, t -> {
            Fx.ring(s.world, Particle.END_ROD, s.at(0, 1, 0), r * (t + 1) / 20.0, 80);
            s.sound(s.at(), "block.glass.break", 10f, 0.5f + t * 0.05f);
        });
    }
}
