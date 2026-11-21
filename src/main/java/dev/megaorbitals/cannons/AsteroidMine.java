package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/** Drops an asteroid packed with ores. It crashes into the ground and stays there for everyone to mine. */
public final class AsteroidMine extends Cannon {
    public AsteroidMine() {
        super("&7&lAsteroid Mine", Material.RAW_IRON_BLOCK, "Drops an asteroid packed with ores", "for players to mine.");
        warning(50, "#A08060");
        option("size", 5.0, "Radius of the asteroid.");
        option("impact-damage", 12.0, "Damage to anyone right under it.");
        option("impact-power", 3.0, "Explosion when it lands (0 = none).");
        option("rock", List.of("STONE:30", "TUFF:15", "DEEPSLATE:15", "ANDESITE:10", "COBBLED_DEEPSLATE:10"), "Filler blocks, MATERIAL:weight.");
        option("ores", List.of("COAL_ORE:12", "IRON_ORE:12", "COPPER_ORE:8", "GOLD_ORE:6", "REDSTONE_ORE:6", "LAPIS_ORE:5",
                "DIAMOND_ORE:4", "EMERALD_ORE:2", "ANCIENT_DEBRIS:1"), "Ore blocks, MATERIAL:weight.");
        option("ore-percent", 35, "Roughly how much of the asteroid is ore.");
    }

    @Override
    public void strike(Strike s) {
        double r = Math.max(2, Math.min(12, d("size")));
        Location ground = s.at();
        Location start = ground.clone().add(-25, 70, -10);
        BlockDisplay rock = Fx.display(start, Material.RAW_IRON_BLOCK, (float) (r * 1.6));
        rock.setTeleportDuration(1);
        s.tag(rock);
        int fall = 40;
        Vector step = ground.toVector().subtract(start.toVector()).multiply(1.0 / fall);
        s.every(1, fall + 1, t -> {
            Location l = start.clone().add(step.clone().multiply(t));
            rock.teleport(l);
            Fx.p(s.world, Particle.FLAME, l, 10, r * 0.4, 0.02);
            Fx.p(s.world, Particle.LARGE_SMOKE, l, 6, r * 0.4, 0.02);
            if (t % 8 == 0) s.sound(l, "entity.blaze.shoot", 4f, 0.4f);
            if (t < fall) return;
            rock.remove();
            land(s, ground, r);
        });
    }

    private void land(Strike s, Location ground, double r) {
        s.soundWorld("entity.generic.explode", 0.7f, 0.5f);
        Fx.p(s.world, Particle.EXPLOSION_EMITTER, ground, 3, r * 0.5, 0);
        Fx.block(s.world, Particle.BLOCK, ground, Material.STONE, 150, r);
        for (LivingEntity le : s.victims(ground, r + 2)) {
            s.damage(le, d("impact-damage"));
            le.setVelocity(Fx.dir(ground, le.getLocation()).multiply(1.2).add(new Vector(0, 0.6, 0)));
        }
        if (d("impact-power") > 0) s.explode(ground.clone().subtract(0, 1, 0), (float) d("impact-power"), false);
        List<Material> rock = weighted(list("rock"));
        List<Material> ores = weighted(list("ores"));
        if (rock.isEmpty()) rock = List.of(Material.STONE);
        List<Material> fRock = rock;
        double orePct = i("ore-percent") / 100.0;
        Location centre = ground.clone().add(0, r * 0.3, 0);
        s.later(4, () -> s.blocks(Blocks.box(centre, (int) Math.ceil(r), (int) Math.ceil(r), (int) Math.ceil(r), (b, dx, dy, dz) -> {
            ThreadLocalRandom rnd = ThreadLocalRandom.current();
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (dist > r - rnd.nextDouble() * 0.9) return;
            boolean ore = !ores.isEmpty() && dist < r - 0.8 && rnd.nextDouble() < orePct;
            List<Material> pool = ore ? ores : fRock;
            Blocks.set(b, pool.get(rnd.nextInt(pool.size())));
        })));
    }

    /** Expands "MATERIAL:weight" entries into a list where each material appears weight times. */
    private static List<Material> weighted(List<String> entries) {
        List<Material> out = new ArrayList<>();
        for (String e : entries) {
            String[] p = e.split(":");
            Material m = Material.matchMaterial(p[0].trim());
            if (m == null || !m.isBlock()) continue;
            int w = 1;
            try { if (p.length > 1) w = Math.max(1, Math.min(100, Integer.parseInt(p[1].trim()))); } catch (NumberFormatException ignored) {}
            for (int k = 0; k < w; k++) out.add(m);
        }
        return out;
    }
}
