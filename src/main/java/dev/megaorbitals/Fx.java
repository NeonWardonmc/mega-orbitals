package dev.megaorbitals;

import net.kyori.adventure.title.Title;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/** Particle, sound and visual helpers shared by all cannons. */
public final class Fx {

    private Fx() {}

    public static Color hex(String hex) {
        try {
            String h = hex.startsWith("#") ? hex.substring(1) : hex;
            return Color.fromRGB(Integer.parseInt(h, 16));
        } catch (Exception e) {
            return Color.RED;
        }
    }

    /** Spawns any particle, filling in whatever data it needs with a sensible default. */
    public static void p(World w, Particle p, Location l, int count, double spread, double speed) {
        Class<?> dt = p.getDataType();
        Object data;
        if (dt == Void.class) data = null;
        else if (dt == Particle.DustOptions.class) data = new Particle.DustOptions(Color.RED, 1.5f);
        else if (dt == Color.class) data = Color.WHITE;
        else if (dt == BlockData.class) data = Material.STONE.createBlockData();
        else if (dt == Float.class) data = 1.0f;
        else if (dt == Integer.class) data = 0;
        else if (dt == ItemStack.class) data = new ItemStack(Material.STONE);
        else if (dt == Particle.DustTransition.class) data = new Particle.DustTransition(Color.RED, Color.ORANGE, 1.5f);
        else return;
        w.spawnParticle(p, l, count, spread, spread, spread, speed, data, true);
    }

    /** A coloured dust particle. */
    public static void dust(World w, Location l, Color c, float size, int count, double spread) {
        w.spawnParticle(Particle.DUST, l, count, spread, spread, spread, 0, new Particle.DustOptions(c, size), true);
    }

    /** A block-coloured particle (BLOCK, FALLING_DUST ...). */
    public static void block(World w, Particle p, Location l, Material m, int count, double spread) {
        w.spawnParticle(p, l, count, spread, spread, spread, 0, m.createBlockData(), true);
    }

    public static List<Location> circle(Location c, double r, int points) {
        List<Location> out = new ArrayList<>();
        for (int i = 0; i < points; i++) {
            double a = 2 * Math.PI * i / points;
            out.add(c.clone().add(Math.cos(a) * r, 0, Math.sin(a) * r));
        }
        return out;
    }

    public static void ring(World w, Particle p, Location c, double r, int points) {
        for (Location l : circle(c, r, points)) p(w, p, l, 1, 0, 0);
    }

    public static void dustRing(World w, Location c, double r, Color col, float size) {
        int points = (int) Math.max(12, r * 5);
        for (Location l : circle(c, r, points)) dust(w, l, col, size, 1, 0);
    }

    public static void line(World w, Particle p, Location a, Location b, double step) {
        Vector dir = b.toVector().subtract(a.toVector());
        double len = dir.length();
        if (len < 0.01) return;
        dir.normalize().multiply(step);
        Location cur = a.clone();
        for (double d = 0; d < len; d += step) {
            p(w, p, cur, 1, 0, 0);
            cur.add(dir);
        }
    }

    public static void dustLine(World w, Location a, Location b, Color col, float size, double step) {
        Vector dir = b.toVector().subtract(a.toVector());
        double len = dir.length();
        if (len < 0.01) return;
        dir.normalize().multiply(step);
        Location cur = a.clone();
        for (double d = 0; d < len; d += step) {
            dust(w, cur, col, size, 1, 0);
            cur.add(dir);
        }
    }

    /** Particles spread evenly over the surface of a sphere. */
    public static void sphere(World w, Particle p, Location c, double r, int points) {
        double golden = Math.PI * (3 - Math.sqrt(5));
        for (int i = 0; i < points; i++) {
            double y = 1 - (i / (double) (points - 1)) * 2;
            double rad = Math.sqrt(1 - y * y);
            double t = golden * i;
            p(w, p, c.clone().add(Math.cos(t) * rad * r, y * r, Math.sin(t) * rad * r), 1, 0, 0);
        }
    }

    public static void dustSphere(World w, Location c, double r, Color col, float size, int points) {
        double golden = Math.PI * (3 - Math.sqrt(5));
        for (int i = 0; i < points; i++) {
            double y = 1 - (i / (double) (points - 1)) * 2;
            double rad = Math.sqrt(1 - y * y);
            double t = golden * i;
            dust(w, c.clone().add(Math.cos(t) * rad * r, y * r, Math.sin(t) * rad * r), col, size, 1, 0);
        }
    }

    /** A big floating block model (used for meteors, bombs ...). Move it with teleport(). */
    public static org.bukkit.entity.BlockDisplay display(Location l, Material m, float scale) {
        return l.getWorld().spawn(l, org.bukkit.entity.BlockDisplay.class, d -> {
            d.setBlock(m.createBlockData());
            d.setTransformation(new org.bukkit.util.Transformation(
                    new org.joml.Vector3f(-scale / 2, -scale / 2, -scale / 2), new org.joml.AxisAngle4f(),
                    new org.joml.Vector3f(scale, scale, scale), new org.joml.AxisAngle4f()));
            d.setTeleportDuration(2);
            d.setBrightness(new org.bukkit.entity.Display.Brightness(15, 15));
        });
    }

    /** A stretched block model, e.g. a flat disc (sx, sy, sz = size on each axis). */
    public static org.bukkit.entity.BlockDisplay display(Location l, Material m, float sx, float sy, float sz) {
        return l.getWorld().spawn(l, org.bukkit.entity.BlockDisplay.class, d -> {
            d.setBlock(m.createBlockData());
            d.setTransformation(new org.bukkit.util.Transformation(
                    new org.joml.Vector3f(-sx / 2, -sy / 2, -sz / 2), new org.joml.AxisAngle4f(),
                    new org.joml.Vector3f(sx, sy, sz), new org.joml.AxisAngle4f()));
            d.setTeleportDuration(2);
            d.setBrightness(new org.bukkit.entity.Display.Brightness(15, 15));
        });
    }

    /** A big floating item model (swords, axes ...). */
    public static org.bukkit.entity.ItemDisplay itemDisplay(Location l, Material m, float scale) {
        return l.getWorld().spawn(l, org.bukkit.entity.ItemDisplay.class, d -> {
            d.setItemStack(new ItemStack(m));
            d.setTransformation(new org.bukkit.util.Transformation(
                    new org.joml.Vector3f(), new org.joml.AxisAngle4f(),
                    new org.joml.Vector3f(scale, scale, scale), new org.joml.AxisAngle4f()));
            d.setTeleportDuration(2);
            d.setBrightness(new org.bukkit.entity.Display.Brightness(15, 15));
        });
    }

    /** Unit vector pointing from a to b (zero if they are the same point). */
    public static Vector dir(Location a, Location b) {
        Vector v = b.toVector().subtract(a.toVector());
        return v.lengthSquared() < 1e-6 ? new Vector() : v.normalize();
    }

    public static void title(Player p, String title, String sub, int stayTicks) {
        p.showTitle(Title.title(MegaOrbitals.color(title), MegaOrbitals.color(sub),
                Title.Times.times(Duration.ofMillis(100), Duration.ofMillis(stayTicks * 50L), Duration.ofMillis(400))));
    }

    /** The classic orbital warning: a shrinking ring on the ground and a targeting beam from the sky. */
    public static void orbitalWarning(Strike s, int ticks, Color color, double radius) {
        if (ticks <= 0) return;
        World w = s.world;
        Location ground = s.at().add(0, 0.2, 0);
        s.sound(ground, "block.beacon.activate", 6f, 0.6f);
        s.every(1, ticks, t -> {
            double progress = (double) t / ticks;
            dustRing(w, ground, Math.max(0.8, radius * (1 - progress)), color, 1.6f);
            if (t % 4 == 0) dustRing(w, ground, radius, color, 1.0f);
            if (t % 2 == 0) {
                for (double y = 0; y < 30; y += 1.0) dust(w, ground.clone().add(0, y, 0), color, 1.2f, 1, 0);
            }
            if (t % 10 == 0) s.sound(ground, "block.note_block.bit", 5f, 0.5f + (float) progress * 1.5f);
        });
    }
}
