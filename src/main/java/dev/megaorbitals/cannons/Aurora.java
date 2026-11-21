package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;

/** Harmless and beautiful: shimmering ribbons of aurora light dance across the sky. */
public final class Aurora extends Cannon {
    public Aurora() {
        super("&a&lAurora", Material.LIGHT_BLUE_STAINED_GLASS, "Harmless and beautiful: shimmering ribbons", "of aurora light dance across the sky.");
        warning(5, "#60FFC0");
        noBlockDamage();
        option("seconds", 60, "How long the aurora lasts.");
        option("height", 40, "Height of the aurora.");
        option("width", 80, "Width of the aurora.");
    }

    @Override
    public void strike(Strike s) {
        int w = i("width"), h = i("height");
        Color[] cols = {Color.fromRGB(60, 255, 140), Color.fromRGB(60, 200, 255), Color.fromRGB(180, 80, 255)};
        s.every(3, i("seconds") * 20 / 3, t -> {
            for (int band = 0; band < 3; band++) {
                for (double x = -w / 2.0; x < w / 2.0; x += 1.5) {
                    double z = Math.sin(x * 0.08 + t * 0.05 + band) * 12 + band * 6;
                    double y = h + Math.sin(x * 0.15 + t * 0.1) * 3;
                    for (int k = 0; k < 3; k++) Fx.dust(s.world, s.at(x, y + k * 1.5, z), cols[band], 2.5f, 1, 0.2);
                }
            }
        });
        s.sound(s.at(), "block.amethyst_block.chime", 6f, 0.5f);
    }
}
