package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/** Teleports every player nearby deep underground, sealed inside an obsidian coffin. */
public final class Bury extends Cannon {
    public Bury() {
        super("&8&lBuried Alive", Material.SOUL_SOIL, "Teleports everyone nearby deep underground,", "sealed in an obsidian coffin.");
        warning(30, "#402020");
        option("radius", 20, "Range.");
        option("depth", 30, "How far underground they go.");
        option("players-only", true, "Only bury players (not mobs).");
    }

    @Override
    public void strike(Strike s) {
        for (LivingEntity le : s.victims(s.at(), d("radius"))) {
            if (b("players-only") && !(le instanceof Player)) continue;
            Location from = le.getLocation();
            int y = Math.max(s.world.getMinHeight() + 3, from.getBlockY() - i("depth"));
            Location to = new Location(s.world, from.getBlockX() + 0.5, y, from.getBlockZ() + 0.5, from.getYaw(), from.getPitch());
            Fx.p(s.world, Particle.SOUL, from, 30, 0.5, 0.05);
            s.sound(from, "entity.enderman.teleport", 4f, 0.5f);
            if (s.breaks()) {
                for (int x = -1; x <= 1; x++) for (int dy = -1; dy <= 2; dy++) for (int z = -1; z <= 1; z++) {
                    Block b = to.clone().add(x, dy, z).getBlock();
                    boolean inside = x == 0 && z == 0 && (dy == 0 || dy == 1);
                    Blocks.set(b, inside ? Material.AIR : Material.OBSIDIAN);
                }
            }
            le.teleport(to);
            le.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0));
            if (le instanceof Player p) Fx.title(p, "&8&lBURIED ALIVE", "&7Start digging.", 60);
        }
    }
}
