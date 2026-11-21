package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;

/** Gravity bomb: pulls everything into the centre, then flips gravity and hurls it all into the sky. */
public final class GravityBomb extends Cannon {
    public GravityBomb() {
        super("&5&lGravity Bomb", Material.CRYING_OBSIDIAN, "Pulls everything into the centre, then", "flips gravity and hurls it all skyward.");
        warning(20, "#6020A0");
        noBlockDamage();
        option("radius", 25, "Range.");
        option("pull-seconds", 3, "How long it pulls.");
        option("launch", 3.5, "Launch strength.");
    }

    @Override
    public void strike(Strike s) {
        Location c = s.at(0, 1, 0);
        int pull = i("pull-seconds") * 20;
        s.every(1, pull, t -> {
            for (LivingEntity le : s.victims(c, d("radius"))) le.setVelocity(Fx.dir(le.getLocation(), c).multiply(0.7));
            Fx.sphere(s.world, Particle.REVERSE_PORTAL, c, d("radius") * (1 - (double) t / pull), 60);
        });
        s.later(pull, () -> {
            for (LivingEntity le : s.victims(c, 8)) le.setVelocity(new Vector((Math.random() - 0.5) * 0.6, d("launch"), (Math.random() - 0.5) * 0.6));
            Fx.p(s.world, Particle.EXPLOSION_EMITTER, c, 1, 0, 0);
            s.sound(c, "entity.wither.break_block", 10f, 0.5f);
        });
    }
}
