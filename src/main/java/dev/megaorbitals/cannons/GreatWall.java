package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.util.Vector;

/** A massive wall rises out of the ground across your line of sight. */
public final class GreatWall extends Cannon {
    public GreatWall() {
        super("&7&lGreat Wall", Material.STONE_BRICKS, "A massive wall rises out of the ground,", "side-on to where you're looking.");
        warning(15, "#A0A0A0");
        option("length", 50, "Length of the wall.");
        option("height", 14, "Height of the wall.");
        option("thickness", 2, "Thickness of the wall.");
        option("material", "STONE_BRICKS", "What the wall is made of.");
    }

    @Override
    public void strike(Strike s) {
        if (!s.breaks()) return;
        Vector look = s.shooter != null ? s.shooter.getLocation().getDirection().setY(0) : new Vector(1, 0, 0);
        if (look.lengthSquared() < 0.01) look = new Vector(1, 0, 0);
        look.normalize();
        Vector along = new Vector(-look.getZ(), 0, look.getX());
        Material m = mat("material");
        Vector lk = look;
        int len = i("length"), h = i("height"), th = i("thickness");
        s.every(2, h, y -> {
            for (int k = -len / 2; k <= len / 2; k++) {
                for (int t = 0; t < th; t++) {
                    Location base = s.surface(s.at().add(along.clone().multiply(k)).add(lk.clone().multiply(t)));
                    Blocks.set(base.add(0, y, 0).getBlock(), m);
                }
            }
            s.sound(s.at(), "block.stone.place", 6f, 0.5f);
        });
    }
}
