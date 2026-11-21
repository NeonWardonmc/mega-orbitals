package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import java.util.*;

/** A field of hovering end crystals. Get too close and they go off, chaining into each other. */
public final class CrystalField extends Cannon {
    public CrystalField() {
        super("&d&lCrystal Minefield", Material.END_CRYSTAL, "A field of hovering end crystals.", "Get close and they chain-detonate.");
        warning(20, "#FF80FF");
        option("radius", 16, "Size of the field.");
        option("spacing", 5, "Distance between crystals.");
        option("trigger-distance", 3.0, "How close sets one off.");
        option("power", 6.0, "Explosion power of each crystal.");
        option("armed-seconds", 120, "Crystals vanish after this long.");
    }

    @Override
    public void strike(Strike s) {
        List<EnderCrystal> crystals = new ArrayList<>();
        int r = i("radius"), sp = Math.max(2, i("spacing"));
        for (int x = -r; x <= r; x += sp) for (int z = -r; z <= r; z += sp) {
            if (x * x + z * z > r * r) continue;
            Location l = s.surface(s.at(x, 0, z)).add(0, 1.5, 0);
            crystals.add(s.tag(s.world.spawn(l, EnderCrystal.class, c -> c.setShowingBottom(false))));
        }
        s.sound(s.at(), "block.end_portal_frame.fill", 8f, 0.6f);
        s.every(5, i("armed-seconds") * 4, t -> {
            Iterator<EnderCrystal> it = crystals.iterator();
            while (it.hasNext()) {
                EnderCrystal c = it.next();
                if (!c.isValid()) { it.remove(); continue; }
                if (!s.victims(c.getLocation(), d("trigger-distance")).isEmpty()) {
                    Location l = c.getLocation();
                    c.remove();
                    it.remove();
                    s.explode(l, (float) d("power"), false);
                }
            }
            if (t == i("armed-seconds") * 4 - 1) crystals.forEach(Entity::remove);
        });
    }
}
