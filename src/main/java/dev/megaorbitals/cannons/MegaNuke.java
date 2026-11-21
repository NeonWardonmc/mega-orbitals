package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import java.util.*;

/**
 * The one and only nuke. 10 second siren, then 100,000 damage to everything
 * in a 16 chunk radius and a crater that eats away the land outward from ground zero.
 */
public final class MegaNuke extends Cannon {

    public MegaNuke() {
        super("&4&l☢ MEGA NUKE ☢", Material.NETHER_STAR,
                "Deals 100,000 damage to everything", "and erases a 16 chunk radius.", "&cThere is no surviving this.");
        warning(200, "#FF0000");
        option("radius", 256, "Blast radius in blocks (16 chunks = 256).");
        option("damage", 100000.0, "Damage dealt to every living thing in the radius.");
        option("crater-depth", 60, "How deep the crater goes at ground zero.");
        option("scorch", true, "Leave scorched blackstone on the crater floor.");
        option("mushroom-cloud-seconds", 20, "How long the mushroom cloud lasts.");
    }

    @Override
    public void warn(Strike s) {
        super.warn(s);
        for (Player p : s.world.getPlayers()) {
            Fx.title(p, "&4&l☢ NUCLEAR LAUNCH DETECTED ☢", "&cGet as far away as you can.", 60);
        }
        s.every(20, warningTicks() / 20, t -> {
            s.soundWorld("entity.ender_dragon.growl", 0.6f, 0.5f);
            s.soundWorld("block.note_block.pling", 1f, t % 2 == 0 ? 0.5f : 0.7f);
            int left = (warningTicks() / 20) - t;
            for (Player p : s.world.getPlayers()) s.actionBar(p, "&4&l☢ IMPACT IN " + left + "s ☢");
        });
    }

    @Override
    public void strike(Strike s) {
        Location gz = s.at();
        int radius = i("radius");
        double damage = d("damage");

        // flash + sound for the whole world
        for (Player p : s.world.getPlayers()) {
            if (p.getLocation().distanceSquared(gz) < 4.0 * radius * radius) {
                Fx.title(p, "&f&l█████████", "", 20);
                p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 0));
            }
        }
        s.soundWorld("entity.generic.explode", 2f, 0.1f);
        s.soundWorld("entity.wither.death", 2f, 0.3f);
        s.soundWorld("entity.lightning_bolt.thunder", 2f, 0.1f);

        // 100,000 damage to everything in range
        for (LivingEntity le : s.victims(gz, radius)) s.damage(le, damage);

        // shockwave ring racing out to the edge
        s.every(1, 60, t -> {
            double r = radius * (t + 1) / 60.0;
            int points = (int) Math.min(240, r * 2);
            for (Location l : Fx.circle(gz.clone().add(0, 2, 0), r, points)) {
                Fx.p(s.world, Particle.EXPLOSION_EMITTER, l, 1, 0, 0);
            }
            for (LivingEntity le : s.victims(gz, r)) {
                Vector away = le.getLocation().toVector().subtract(gz.toVector()).setY(0);
                if (away.lengthSquared() > 0.01) le.setVelocity(away.normalize().multiply(3).setY(1.5));
            }
        });

        // the crater: removes everything above a bowl-shaped floor, nearest columns first
        int depth = i("crater-depth");
        boolean scorch = b("scorch");
        World w = s.world;
        int gy = gz.getBlockY();
        Random rnd = new Random();
        s.blocks(Blocks.columns(gz, radius, (x, z, dist) -> {
            if (!Blocks.loaded(w, x, z)) return 1;
            int floor = (int) (gy - depth * Math.sqrt(Math.max(0, 1 - (dist / radius) * (dist / radius))));
            floor = Math.max(w.getMinHeight() + 1, floor);
            int top = w.getHighestBlockYAt(x, z);
            int work = 1;
            for (int y = top; y > floor; y--) {
                Block b = w.getBlockAt(x, y, z);
                if (!b.getType().isAir()) Blocks.set(b, Material.AIR);
                work++;
            }
            if (scorch) {
                Block f = w.getBlockAt(x, floor, z);
                if (!f.getType().isAir() && rnd.nextInt(3) == 0) Blocks.set(f, rnd.nextBoolean() ? Material.BLACKSTONE : Material.BASALT);
                if (dist < radius * 0.15 && rnd.nextInt(6) == 0) Blocks.set(f, Material.MAGMA_BLOCK);
            }
            return work;
        }));

        // mushroom cloud
        int seconds = i("mushroom-cloud-seconds");
        s.every(4, seconds * 5, t -> {
            double rise = Math.min(1.0, t / 25.0);
            double stemTop = 40 + 60 * rise;
            for (double y = 0; y < stemTop; y += 3) {
                Fx.p(w, Particle.CAMPFIRE_SIGNAL_SMOKE, gz.clone().add(0, y, 0), 3, 3, 0.02);
                if (y < 20) Fx.p(w, Particle.FLAME, gz.clone().add(0, y, 0), 4, 3, 0.05);
            }
            for (double r = 6; r < 10 + 30 * rise; r += 5) {
                for (Location l : Fx.circle(gz.clone().add(0, stemTop, 0), r, (int) (r * 1.5))) {
                    Fx.p(w, Particle.CAMPFIRE_SIGNAL_SMOKE, l, 2, 2, 0.01);
                    Fx.p(w, Particle.LAVA, l, 1, 2, 0);
                }
            }
        });
    }
}
