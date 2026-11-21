package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.Player;
import java.util.*;

/** Sets up an orbital defence grid. Strikes fired into its area by other players are shot out of the sky before they land. */
public final class DefenceGrid extends Cannon implements StrikeInterceptor {

    private static final class Grid {
        final Strike s;
        final Location centre;
        final double radius;
        final UUID owner;
        final long until;
        int shotsLeft;
        Grid(Strike s, double radius, int shots, long until) {
            this.s = s;
            this.centre = s.at();
            this.radius = radius;
            this.owner = s.shooter == null ? null : s.shooter.getUniqueId();
            this.shotsLeft = shots;
            this.until = until;
        }
    }

    private final List<Grid> grids = new ArrayList<>();

    public DefenceGrid() {
        super("&a&lOrbital Defence Grid", Material.LIGHTNING_ROD, "Protects an area: other players' orbital", "strikes there are shot down before they land.");
        warning(20, "#40FF80");
        noBlockDamage();
        option("radius", 40.0, "Size of the protected area.");
        option("seconds", 600, "How long the grid stays up.");
        option("interceptions", 5, "How many strikes it can shoot down before it burns out (0 = unlimited).");
        option("block-own-strikes", false, "Also shoot down strikes fired by whoever set up the grid.");
    }

    @Override
    public void strike(Strike s) {
        int shots = i("interceptions");
        long until = System.currentTimeMillis() + i("seconds") * 1000L;
        Grid g = new Grid(s, d("radius"), shots <= 0 ? Integer.MAX_VALUE : shots, until);
        grids.add(g);
        Color col = Color.fromRGB(0x40FF80);
        s.sound(g.centre, "block.beacon.activate", 6f, 1.6f);
        s.tell("&aDefence grid online for " + i("seconds") + "s.");
        s.loop(20, t -> {
            if (!grids.contains(g)) return false;
            if (System.currentTimeMillis() > g.until || g.shotsLeft <= 0) {
                grids.remove(g);
                s.sound(g.centre, "block.beacon.deactivate", 6f, 1f);
                s.tell("&7Your defence grid has gone offline.");
                return false;
            }
            if (t % 3 == 0) Fx.dustRing(s.world, g.centre.clone().add(0, 0.3, 0), g.radius, col, 1.4f);
            for (Location l : Fx.circle(g.centre, 2, 4)) Fx.dustLine(s.world, l, l.clone().add(0, 6, 0), col, 1f, 1);
            Fx.p(s.world, Particle.END_ROD, g.centre.clone().add(0, 6.5, 0), 2, 0.2, 0.01);
            return true;
        });
    }

    @Override
    public boolean intercept(Strike in) {
        long now = System.currentTimeMillis();
        grids.removeIf(g -> now > g.until || g.shotsLeft <= 0);
        Location hit = in.at();
        for (Grid g : grids) {
            if (g.centre.getWorld() != hit.getWorld() || g.centre.distance(hit) > g.radius) continue;
            boolean own = g.owner != null && in.shooter != null && in.shooter.getUniqueId().equals(g.owner);
            if (own && !b("block-own-strikes")) continue;
            g.shotsLeft--;
            shootDown(g, in);
            return true;
        }
        return false;
    }

    private void shootDown(Grid g, Strike in) {
        World w = g.centre.getWorld();
        Location turret = g.centre.clone().add(0, 6.5, 0);
        Location boom = in.at().add(0, 35, 0);
        Fx.dustLine(w, turret, boom, Color.fromRGB(0x60FF90), 1.8f, 0.5);
        Fx.p(w, Particle.EXPLOSION_EMITTER, boom, 2, 1, 0);
        Fx.p(w, Particle.FIREWORK, boom, 60, 1.5, 0.2);
        w.playSound(turret, "entity.firework_rocket.blast", 4f, 0.6f);
        w.playSound(boom, "entity.generic.explode", 8f, 1.2f);
        String name = in.cannon.name();
        String left = g.shotsLeft > 1000 ? "" : " &7(" + g.shotsLeft + " shots left)";
        if (in.shooter != null && in.shooter.isOnline())
            in.shooter.sendActionBar(MegaOrbitals.color("&c&lINTERCEPTED! &7Your " + name + " &7was shot down by a defence grid."));
        Player owner = g.owner == null ? null : Bukkit.getPlayer(g.owner);
        if (owner != null) owner.sendActionBar(MegaOrbitals.color("&a&lINTERCEPTED &7" + name + left));
    }
}
