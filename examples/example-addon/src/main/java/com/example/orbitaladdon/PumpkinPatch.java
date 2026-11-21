package com.example.orbitaladdon;

import dev.megaorbitals.Blocks;
import dev.megaorbitals.Cannon;
import dev.megaorbitals.Fx;
import dev.megaorbitals.Strike;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Grows a field of pumpkins and jack o'lanterns. Shows how to change blocks safely:
 * through Blocks.set inside an s.blocks(...) job, so /orbital undo can put everything back.
 */
public final class PumpkinPatch extends Cannon {

    public PumpkinPatch() {
        super("&6&lPumpkin Patch", Material.CARVED_PUMPKIN, "Grows a field of pumpkins and", "glowing jack o'lanterns.");
        warning(30, "#FF9020");
        catalog("rare", "harvest");       // "harvest" is a new category, so it becomes a new menu tab
        option("radius", 10, "Size of the patch.");
        option("chance", 0.25, "Chance of a pumpkin on each block (0 to 1).");
    }

    // Override id() if your class name might clash with another cannon. The default is the class name in lower case.
    // @Override public String id() { return "example_pumpkinpatch"; }

    @Override
    public void strike(Strike s) {
        if (!s.breaks()) {                // respect break-blocks: false
            s.tell("&cPumpkin Patch needs break-blocks: true.");
            return;
        }
        World w = s.world;
        double chance = d("chance");
        // Blocks.columns visits every x/z column, nearest first, so the patch visibly spreads outward.
        // s.blocks(...) spreads the work over several ticks and records every change for /orbital undo.
        s.blocks(Blocks.throttle(Blocks.columns(s.at(), i("radius"), (x, z, dist) -> {
            if (!Blocks.loaded(w, x, z)) return 1;
            Location top = Blocks.surface(new Location(w, x + 0.5, s.at().getY(), z + 0.5));
            Block ground = top.getBlock().getRelative(0, -1, 0);
            Block above = top.getBlock();
            if (ThreadLocalRandom.current().nextDouble() > chance || !above.getType().isAir()) return 1;
            if (ground.getType() == Material.GRASS_BLOCK || ground.getType() == Material.DIRT) Blocks.set(ground, Material.FARMLAND);
            Blocks.set(above, ThreadLocalRandom.current().nextInt(4) == 0 ? Material.JACK_O_LANTERN : Material.PUMPKIN);
            Fx.p(w, Particle.HAPPY_VILLAGER, above.getLocation().add(0.5, 1, 0.5), 3, 0.3, 0);
            return 4;
        }), 30));
        s.sound(s.at(), "block.crop.break", 3f, 0.6f);
    }
}
