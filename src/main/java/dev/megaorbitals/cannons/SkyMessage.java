package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

/** Writes a giant glowing message in the sky above the target. Edit the text in the config. */
public final class SkyMessage extends Cannon {
    public SkyMessage() {
        super("&e&lSky Writer", Material.OAK_SIGN, "Writes a giant glowing message in the sky.", "Edit the text in the config.");
        warning(5, "#FFFF60");
        noBlockDamage();
        option("message", "&c&l{player} WAS HERE", "Text to write ({player} = who fired it).");
        option("height", 25, "Height above the target.");
        option("size", 12.0, "Text size.");
        option("seconds", 120, "How long it stays.");
    }

    @Override
    public void strike(Strike s) {
        String msg = str("message").replace("{player}", s.shooter == null ? "?" : s.shooter.getName());
        float size = (float) d("size");
        TextDisplay td = s.world.spawn(s.at(0, i("height"), 0), TextDisplay.class, t -> {
            t.text(s.text(msg));
            t.setBillboard(Display.Billboard.CENTER);
            t.setBrightness(new Display.Brightness(15, 15));
            t.setShadowed(true);
            t.setTransformation(new Transformation(new Vector3f(), new AxisAngle4f(), new Vector3f(size, size, size), new AxisAngle4f()));
            t.setViewRange(4f);
        });
        s.expire(s.tag(td), i("seconds") * 20L);
        s.sound(s.at(), "block.note_block.chime", 6f, 1f);
    }
}
