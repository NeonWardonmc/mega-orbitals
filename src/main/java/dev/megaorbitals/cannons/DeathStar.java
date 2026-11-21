package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;

/** A massive green superlaser that vaporises a shaft all the way to bedrock. */
public final class DeathStar extends Cannon {
    public DeathStar() {
        super("&a&lSuperlaser", Material.SLIME_BLOCK, "A giant green beam from orbit that", "vaporises a shaft down to bedrock.");
        warning(60, "#30FF30");
        option("radius", 10, "Radius of the beam.");
        option("damage", 1000.0, "Damage to anything inside the beam.");
        option("seconds", 6, "How long the beam lasts.");
    }

    @Override
    public void strike(Strike s) {
        int r = i("radius");
        Color green = Color.fromRGB(60, 255, 60);
        Location c = s.at();
        World w = s.world;
        s.blocks(Blocks.columns(c, r, (x, z, dist) -> {
            if (!Blocks.loaded(w, x, z)) return 1;
            int top = w.getHighestBlockYAt(x, z);
            for (int y = top; y > w.getMinHeight(); y--) {
                Block b = w.getBlockAt(x, y, z);
                if (!b.getType().isAir()) Blocks.set(b, Material.AIR);
            }
            return top - w.getMinHeight();
        }));
        s.soundWorld("entity.warden.sonic_boom", 1f, 0.5f);
        s.every(2, i("seconds") * 10, t -> {
            for (double y = c.getY() + 80; y > w.getMinHeight(); y -= 2) {
                Location l = new Location(w, c.getX(), y, c.getZ());
                Fx.dustRing(w, l, r * (0.6 + 0.4 * Math.random()), green, 3f);
                Fx.dust(w, l, Color.WHITE, 3f, 3, r * 0.3);
            }
            for (Entity e : w.getNearbyEntities(c, r, 400, r)) {
                if (e instanceof LivingEntity le && !s.spared(e)) s.damage(le, d("damage"));
            }
            if (t % 10 == 0) s.sound(c, "block.beacon.activate", 8f, 0.5f);
        });
    }
}
