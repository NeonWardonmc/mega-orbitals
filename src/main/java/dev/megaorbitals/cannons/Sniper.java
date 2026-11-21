package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;

/** A red laser sight locks onto the player nearest the target. Three seconds later: one shot. */
public final class Sniper extends Cannon {
    public Sniper() {
        super("&4&lOrbital Sniper", Material.CROSSBOW, "A red laser sight locks onto the nearest player.", "Three seconds later: one devastating shot.");
        warning(5, "#FF0000");
        noBlockDamage();
        option("range", 50, "How far from the target to find someone.");
        option("aim-seconds", 3, "How long the laser sight tracks before firing.");
        option("damage", 40.0, "Damage of the shot.");
    }

    @Override
    public void strike(Strike s) {
        Player victim = s.nearestPlayer(s.at(), d("range"));
        if (victim == null) {
            if (s.shooter != null) s.actionBar(s.shooter, "&cNo target for the sniper.");
            return;
        }
        Fx.title(victim, "&4&l⌖", "&cYou're in someone's sights.", 30);
        Color red = Color.fromRGB(255, 0, 0);
        int aim = i("aim-seconds") * 20;
        s.every(1, aim + 1, t -> {
            if (!victim.isOnline() || victim.isDead()) return;
            Location head = victim.getEyeLocation();
            Location sky = head.clone().add(20, 80, 20);
            if (t % 2 == 0) Fx.dustLine(s.world, sky, head, red, 0.6f, 1.0);
            if (t % 10 == 0) victim.playSound(head, "block.note_block.bit", 1f, 2f);
            if (t != aim) return;
            Fx.line(s.world, Particle.END_ROD, sky, head, 0.5);
            s.world.strikeLightningEffect(victim.getLocation());
            s.damage(victim, d("damage"));
            victim.setVelocity(Fx.dir(sky, head).multiply(1.5).add(new Vector(0, 0.5, 0)));
            s.soundWorld("entity.generic.explode", 0.5f, 2f);
        });
    }
}
