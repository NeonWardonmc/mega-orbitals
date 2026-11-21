package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.*;
import org.bukkit.entity.*;

/** Plants a giant bomb with a boss bar countdown everyone can see. When it hits zero... */
public final class DoomClock extends Cannon {
    public DoomClock() {
        super("&4&lDoom Clock", Material.CLOCK, "Plants a giant bomb with a", "countdown everyone can see.");
        warning(20, "#AA0000");
        option("seconds", 60, "Countdown length.");
        option("power", 25.0, "Power of the main blast.");
        option("ring-blasts", 10, "Extra blasts in a ring around it.");
        option("ring-radius", 16.0, "Radius of the ring blasts.");
    }

    @Override
    public void strike(Strike s) {
        int secs = i("seconds");
        Location c = s.at(0, 1.5, 0);
        BlockDisplay bomb = Fx.display(c, Material.TNT, 3f);
        TextDisplay label = s.world.spawn(c.clone().add(0, 2.5, 0), TextDisplay.class, t -> t.setBillboard(Display.Billboard.CENTER));
        BossBar bar = BossBar.bossBar(s.text("&4&lDOOM CLOCK"), 1f, BossBar.Color.RED, BossBar.Overlay.NOTCHED_20);
        for (Player p : s.world.getPlayers()) p.showBossBar(bar);
        s.every(20, secs + 1, t -> {
            int left = secs - t;
            bar.progress(Math.max(0f, left / (float) secs));
            bar.name(s.text("&4&lDOOM CLOCK &8» &c" + left + "s"));
            label.text(s.text("&4&l" + left));
            s.sound(c, "block.note_block.hat", 6f, left <= 10 ? 2f : 1f);
            if (left <= 10) s.soundWorld("block.note_block.pling", 0.6f, 0.5f);
            if (left > 0) return;
            for (Player p : s.world.getPlayers()) p.hideBossBar(bar);
            bomb.remove();
            label.remove();
            s.explode(c, (float) d("power"), true);
            for (Location l : Fx.circle(c, d("ring-radius"), i("ring-blasts"))) {
                s.later(5, () -> { s.explode(l, 6f, false); s.world.strikeLightningEffect(l); });
            }
        });
    }
}
