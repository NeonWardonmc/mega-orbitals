package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import java.util.*;

/** The area becomes a spider nest: cobwebs everywhere, spiders, cave spiders and spider jockeys. */
public final class SpiderNest extends Cannon {
    public SpiderNest() {
        super("&8&lSpider Nest", Material.COBWEB, "The area becomes a spider nest: webs", "everywhere, spiders and spider jockeys.");
        warning(20, "#404040");
        option("radius", 12, "Size of the nest.");
        option("webs", 60, "Cobwebs placed.");
        option("spiders", 6, "Spiders.");
        option("cave-spiders", 8, "Cave spiders.");
        option("jockeys", 2, "Spider jockeys.");
    }

    @Override
    public void strike(Strike s) {
        Random rnd = new Random();
        double r = d("radius");
        if (s.breaks()) {
            for (int k = 0; k < i("webs"); k++) {
                Block b = s.randomPoint(r).add(0, rnd.nextInt(3), 0).getBlock();
                if (b.getType().isAir()) Blocks.put(b, Material.COBWEB, false);
            }
        }
        for (int k = 0; k < i("spiders"); k++) s.hunt(s.mob(s.randomPoint(r), Spider.class, null), 40);
        for (int k = 0; k < i("cave-spiders"); k++) s.hunt(s.mob(s.randomPoint(r), CaveSpider.class, null), 40);
        for (int k = 0; k < i("jockeys"); k++) {
            Location l = s.randomPoint(r);
            Spider sp = s.mob(l, Spider.class, null);
            sp.addPassenger(s.mob(l, Skeleton.class, null));
            s.hunt(sp, 40);
        }
        s.sound(s.at(), "entity.spider.ambient", 8f, 0.6f);
    }
}
