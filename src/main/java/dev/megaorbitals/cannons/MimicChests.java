package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Chest;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/** Scatters tempting loot chests. Open one and it bites, blows up or lets out a swarm of mobs. */
public final class MimicChests extends Cannon {

    private final Map<Block, Strike> mimics = new HashMap<>();

    public MimicChests() {
        super("&6&lMimic Chests", Material.CHEST, "Scatters tempting loot chests around the target.", "Open one and it bites, explodes or lets out mobs!");
        warning(20, "#C08030");
        option("chests", 8, "How many mimic chests appear.");
        option("radius", 14.0, "How far apart they are scattered.");
        option("bite-damage", 8.0, "Damage of a bite.");
        option("explode-power", 3.0, "Power of an exploding mimic.");
        option("mobs", 4, "Mobs that pour out of a mob mimic.");
        option("seconds", 300, "Unopened mimics vanish after this long.");
    }

    @Override
    public void strike(Strike s) {
        if (!s.breaks()) { s.tell("&cMimic Chests needs break-blocks: true to place chests."); return; }
        List<Block> placed = new ArrayList<>();
        for (int k = 0; k < i("chests"); k++) {
            Block b = s.randomPoint(d("radius")).getBlock();
            if (!b.getType().isAir() || mimics.containsKey(b)) continue;
            Chest data = (Chest) Material.CHEST.createBlockData();
            data.setFacing(new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST}[ThreadLocalRandom.current().nextInt(4)]);
            Blocks.put(b, data, false);
            mimics.put(b, s);
            placed.add(b);
            Fx.p(s.world, Particle.HAPPY_VILLAGER, b.getLocation().add(0.5, 1, 0.5), 10, 0.4, 0);
        }
        s.sound(s.at(), "block.chest.close", 3f, 0.6f);
        s.later(i("seconds") * 20L, () -> {
            for (Block b : placed) {
                if (mimics.remove(b) != null && b.getType() == Material.CHEST) Blocks.put(b, Material.AIR, false);
            }
        });
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onOpen(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getClickedBlock() == null) return;
        Strike s = mimics.get(e.getClickedBlock());
        if (s == null) return;
        e.setCancelled(true);
        trigger(s, e.getClickedBlock(), e.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onBreak(BlockBreakEvent e) {
        Strike s = mimics.get(e.getBlock());
        if (s == null) return;
        e.setCancelled(true);
        trigger(s, e.getBlock(), e.getPlayer());
    }

    private void trigger(Strike s, Block b, Player p) {
        mimics.remove(b);
        Location c = b.getLocation().add(0.5, 0.5, 0.5);
        s.run(() -> Blocks.put(b, Material.AIR, false));
        int roll = ThreadLocalRandom.current().nextInt(3);
        if (roll == 0) {
            Fx.title(p, "&6&lMIMIC!", "&7It bit you!", 30);
            s.sound(c, "entity.evoker_fangs.attack", 2f, 0.6f);
            s.sound(c, "entity.ravager.attack", 2f, 1.4f);
            Fx.block(s.world, Particle.BLOCK, c, Material.OAK_PLANKS, 30, 0.4);
            if (!s.spared(p)) {
                s.damage(p, d("bite-damage"));
                p.setVelocity(Fx.dir(c, p.getLocation()).multiply(0.9).add(new Vector(0, 0.4, 0)));
            }
        } else if (roll == 1) {
            Fx.title(p, "&c&lMIMIC!", "&7...it was a bomb.", 30);
            s.sound(c, "entity.tnt.primed", 2f, 1f);
            s.later(10, () -> s.explode(c, (float) d("explode-power"), false));
        } else {
            Fx.title(p, "&2&lMIMIC!", "&7Something crawls out...", 30);
            s.sound(c, "entity.zombie.ambient", 2f, 0.6f);
            Fx.p(s.world, Particle.LARGE_SMOKE, c, 30, 0.5, 0.02);
            for (int k = 0; k < i("mobs"); k++) {
                Class<? extends Mob> type = switch (ThreadLocalRandom.current().nextInt(4)) {
                    case 0 -> Zombie.class;
                    case 1 -> Spider.class;
                    case 2 -> Silverfish.class;
                    default -> Skeleton.class;
                };
                Mob m = s.mob(c, type, null);
                m.setVelocity(new Vector(Math.random() - 0.5, 0.5, Math.random() - 0.5).multiply(0.6));
                if (!s.spared(p)) m.setTarget(p);
            }
        }
    }
}
