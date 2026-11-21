package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/** Plants fake diamond ore in the ground. Anyone who mines it gets a nasty surprise. */
public final class BoobyTrap extends Cannon {

    private record Trap(Strike strike, Material original) {}

    private final Map<Block, Trap> traps = new HashMap<>();

    public BoobyTrap() {
        super("&b&lBooby-Trapped Ores", Material.DIAMOND_ORE, "Plants fake diamond ore around the target.", "Mine it and it explodes in your face!");
        warning(20, "#40E0E0");
        option("ores", 12, "How many fake ores are planted.");
        option("radius", 12.0, "How far they are spread.");
        option("max-depth", 3, "Ores can be buried up to this many blocks under the surface (0 = all on top).");
        option("power", 4.0, "Explosion power when one is mined.");
        option("armed-minutes", 30, "Unmined ores turn back into what they were after this long.");
    }

    @Override
    public void strike(Strike s) {
        if (!s.breaks()) { s.tell("&cBooby-Trapped Ores needs break-blocks: true."); return; }
        List<Block> planted = new ArrayList<>();
        for (int k = 0; k < i("ores"); k++) {
            Location top = s.randomPoint(d("radius"));
            int depth = ThreadLocalRandom.current().nextInt(Math.max(0, i("max-depth")) + 1);
            Block b = top.getBlock().getRelative(0, -1 - depth, 0);
            if (!b.getType().isSolid() || !Blocks.breakable(b) || traps.containsKey(b) || b.getState() instanceof org.bukkit.block.TileState) continue;
            Material orig = b.getType();
            boolean deep = orig.name().contains("DEEPSLATE") || orig == Material.TUFF;
            Blocks.set(b, deep ? Material.DEEPSLATE_DIAMOND_ORE : Material.DIAMOND_ORE);
            traps.put(b, new Trap(s, orig));
            planted.add(b);
        }
        s.sound(s.at(), "block.amethyst_block.chime", 3f, 0.6f);
        s.tell("&b" + planted.size() + " booby traps planted.");
        s.later(i("armed-minutes") * 1200L, () -> {
            for (Block b : planted) {
                Trap t = traps.remove(b);
                if (t != null && (b.getType() == Material.DIAMOND_ORE || b.getType() == Material.DEEPSLATE_DIAMOND_ORE)) Blocks.put(b, t.original(), false);
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onMine(BlockBreakEvent e) {
        Trap t = traps.remove(e.getBlock());
        if (t == null) return;
        e.setDropItems(false);
        e.setExpToDrop(0);
        Strike s = t.strike();
        Location c = e.getBlock().getLocation().add(0.5, 0.5, 0.5);
        Fx.title(e.getPlayer(), "&c&lBOOBY TRAP!", "&7That wasn't a diamond...", 30);
        s.sound(c, "entity.tnt.primed", 2f, 1.6f);
        s.later(6, () -> s.explode(c, (float) d("power"), false));
    }
}
