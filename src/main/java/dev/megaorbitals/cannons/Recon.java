package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/** A recon satellite scans a huge area: everyone is revealed, glowing, and you get a full report. */
public final class Recon extends Cannon {
    public Recon() {
        super("&a&lRecon Scan", Material.RECOVERY_COMPASS, "A recon satellite scans a huge area. Everyone", "glows, and you get their names and positions.");
        warning(10, "#40FF80");
        noBlockDamage();
        option("radius", 300, "Scan radius.");
        option("glow-seconds", 60, "How long they glow.");
        option("reveal-invisible", true, "Remove invisibility from anyone scanned.");
    }

    @Override
    public void strike(Strike s) {
        double r = d("radius");
        int found = 0;
        if (s.shooter != null) s.shooter.sendMessage(s.text("&a&l[RECON] &7Scan results:"));
        for (Player p : s.world.getPlayers()) {
            if (s.spared(p) || p.getLocation().distance(s.at()) > r) continue;
            if (b("reveal-invisible")) p.removePotionEffect(PotionEffectType.INVISIBILITY);
            p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, i("glow-seconds") * 20, 0));
            found++;
            if (s.shooter != null) {
                Location l = p.getLocation();
                s.shooter.sendMessage(s.text("  &f" + p.getName() + " &7at &f" + l.getBlockX() + ", " + l.getBlockY() + ", " + l.getBlockZ()
                        + " &8(" + (int) l.distance(s.at()) + "m) &c❤" + (int) p.getHealth()));
            }
            p.playSound(p.getLocation(), "block.note_block.bit", 1f, 0.5f);
        }
        if (s.shooter != null && found == 0) s.shooter.sendMessage(s.text("  &7Nobody found."));
        for (int k = 0; k < 4; k++) {
            int kk = k;
            s.later(k * 5L, () -> Fx.ring(s.world, Particle.HAPPY_VILLAGER, s.at(0, 1, 0), 10 + kk * 15, 120));
        }
        s.sound(s.at(), "block.beacon.power_select", 8f, 2f);
    }
}
