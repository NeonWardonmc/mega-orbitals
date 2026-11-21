package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import java.util.*;

/** A tractor beam lifts anyone who steps into it up to a floating sky platform. Drop through the hole to float back down. */
public final class OrbitalElevator extends Cannon {
    public OrbitalElevator() {
        super("&f&lOrbital Elevator", Material.END_ROD, "A beam lifts players up to a floating sky", "platform. Drop through the hole to float down.");
        warning(20, "#E0F0FF");
        option("height", 60, "How high above the ground the platform floats.");
        option("platform-radius", 6, "Size of the sky platform.");
        option("seconds", 180, "How long the elevator runs before the platform disappears.");
        option("lift-speed", 0.7, "How fast players rise.");
    }

    @Override
    public void strike(Strike s) {
        if (!s.breaks()) { s.tell("&cOrbital Elevator needs break-blocks: true to build its platform."); return; }
        Location ground = s.at();
        int top = Math.min(s.world.getMaxHeight() - 4, ground.getBlockY() + i("height"));
        if (top - ground.getBlockY() < 10) { s.tell("&cNot enough room above the target for a sky platform."); return; }
        int r = Math.max(3, i("platform-radius"));
        int cx = ground.getBlockX(), cz = ground.getBlockZ();
        List<Block> placed = new ArrayList<>();
        for (int x = -r; x <= r; x++) for (int z = -r; z <= r; z++) {
            double dd = Math.sqrt(x * x + z * z);
            if (dd > r + 0.4) continue;
            boolean hole = Math.abs(x) <= 1 && Math.abs(z) <= 1;
            Block floor = s.world.getBlockAt(cx + x, top, cz + z);
            if (!hole && floor.getType().isAir()) {
                Material m = (Math.abs(x) == 2 && Math.abs(z) <= 2) || (Math.abs(z) == 2 && Math.abs(x) <= 2) ? Material.YELLOW_STAINED_GLASS
                        : (x == 0 || z == 0) && dd > r - 1.5 ? Material.SEA_LANTERN : Material.WHITE_STAINED_GLASS;
                Blocks.set(floor, m);
                placed.add(floor);
            }
            if (dd > r - 0.6) {
                Block rail = floor.getRelative(0, 1, 0);
                if (rail.getType().isAir()) { Blocks.set(rail, Material.GLASS_PANE); placed.add(rail); }
            }
        }
        double topY = top + 1;
        Location beamBase = ground.clone();
        Color col = Color.fromRGB(0xE0F0FF);
        Set<UUID> rising = new HashSet<>();
        Map<UUID, Integer> rest = new HashMap<>();
        s.sound(ground, "block.beacon.activate", 6f, 1.2f);
        int ticks = i("seconds") * 20;
        s.every(1, ticks + 1, t -> {
            if (t % 2 == 0) for (double y = ground.getY(); y < topY; y += 1.5)
                Fx.dustRing(s.world, new Location(s.world, beamBase.getX(), y + (t % 6) * 0.25, beamBase.getZ()), 1.2, col, 1.0f);
            if (t == ticks) {
                for (Player p : s.world.getPlayers()) {
                    Location l = p.getLocation();
                    if (Math.abs(l.getX() - beamBase.getX()) <= r + 1 && Math.abs(l.getZ() - beamBase.getZ()) <= r + 1 && l.getY() > ground.getY() + 5)
                        p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 20 * 30, 0));
                }
                for (Block b : placed) if (!b.getType().isAir()) Blocks.put(b, Material.AIR, false);
                s.sound(ground, "block.beacon.deactivate", 6f, 1f);
                return;
            }
            for (Player p : s.world.getPlayers()) {
                Location l = p.getLocation();
                double dx = l.getX() - beamBase.getX(), dz = l.getZ() - beamBase.getZ();
                if (dx * dx + dz * dz > 1.6 * 1.6 || l.getY() < ground.getY() - 1 || l.getY() > topY + 3) {
                    rising.remove(p.getUniqueId());
                    continue;
                }
                UUID id = p.getUniqueId();
                if (rising.contains(id)) {
                    if (l.getY() >= topY + 0.3) {
                        rising.remove(id);
                        Location land = new Location(s.world, beamBase.getX() + 3, topY, beamBase.getZ(), l.getYaw(), l.getPitch());
                        p.teleport(land);
                        p.setFallDistance(0);
                        s.sound(land, "entity.player.levelup", 1f, 1.6f);
                        s.actionBar(p, "&fWelcome to the sky platform. &7Drop through the hole to go down.");
                    } else {
                        p.setVelocity(new Vector(-dx * 0.2, d("lift-speed"), -dz * 0.2));
                        p.setFallDistance(0);
                    }
                } else if (l.getY() < ground.getY() + 2.5 && p.isOnGround()) {
                    Integer last = rest.get(id);
                    if (last == null || t - last > 60) {
                        rising.add(id);
                        s.sound(l, "block.beacon.power_select", 1f, 1.8f);
                    }
                } else if (!p.isOnGround() && l.getY() < topY) {
                    p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 10, 0, false, false));
                    p.setFallDistance(0);
                    rest.put(id, t);
                }
            }
        });
    }
}
