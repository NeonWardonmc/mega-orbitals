package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;

/** An irresistible song lures everyone to the centre... where a bomb is waiting. */
public final class SirenSong extends Cannon {
    public SirenSong() {
        super("&b&lSiren Song", Material.NOTE_BLOCK, "An irresistible song lures everyone", "to the centre... where a bomb waits.");
        warning(10, "#60D0FF");
        option("radius", 35, "Range of the song.");
        option("seconds", 10, "How long they are lured.");
        option("pull", 0.3, "How fast they walk in.");
        option("power", 6.0, "Power of the bomb at the end.");
    }

    @Override
    public void strike(Strike s) {
        Location c = s.at();
        s.every(2, i("seconds") * 10, t -> {
            Fx.p(s.world, Particle.NOTE, c.clone().add(0, 2, 0), 3, 1, 1);
            for (LivingEntity le : s.victims(c, d("radius"))) {
                Vector in = Fx.dir(le.getLocation(), c).setY(0);
                if (in.lengthSquared() < 0.01) continue;
                le.setVelocity(in.normalize().multiply(d("pull")).setY(le.getVelocity().getY()));
                if (t % 10 == 0) Fx.p(s.world, Particle.NOTE, le.getLocation().add(0, 2.2, 0), 1, 0, 1);
            }
            if (t % 5 == 0) s.sound(c, "block.note_block.chime", 6f, 0.5f + (t % 20) * 0.075f);
        });
        s.later(i("seconds") * 20L, () -> s.explode(c, (float) d("power"), false));
    }
}
