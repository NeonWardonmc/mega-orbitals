package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/** Builds a floating sky base high above the target: glass walls, a domed roof and a landing pad. */
public final class SpaceStation extends Cannon {
    public SpaceStation() {
        super("&b&lSpace Station", Material.BEACON, "Builds a floating sky base above the target", "with glass walls and a landing pad.");
        warning(30, "#60C0FF");
        option("height", 50, "How high above the target the station floats.");
        option("radius", 7, "Radius of the main room.");
        option("teleport-shooter", true, "Beam the shooter up to the landing pad once it is built.");
        option("build-speed", 600, "Blocks placed per tick (lower = slower, more dramatic build).");
    }

    @Override
    protected double warningRadius() { return 6; }

    @Override
    public void strike(Strike s) {
        if (!s.breaks()) { s.tell("&cSpace Station needs break-blocks: true."); return; }
        int r = Math.max(4, Math.min(16, i("radius")));
        int y0 = Math.min(s.world.getMaxHeight() - 10, s.at().getBlockY() + i("height"));
        Location c = new Location(s.world, s.at().getBlockX(), y0, s.at().getBlockZ());
        int padMid = r + 6;
        s.blocks(Blocks.throttle(Blocks.box(c, r + 10, 8, r, (b, dx, dy, dz) -> {
            Material m = pick(r, padMid, dx, dy, dz);
            if (m != null) Blocks.set(b, m);
        }), Math.max(50, i("build-speed"))));
        Fx.dustLine(s.world, s.at(), c.clone().add(0.5, 0, 0.5), Color.AQUA, 2f, 1);
        s.sound(s.at(), "block.beacon.activate", 6f, 0.8f);
        if (b("teleport-shooter") && s.shooter != null) {
            s.later(120, () -> {
                if (!s.shooter.isOnline()) return;
                Location pad = c.clone().add(padMid + 0.5, 0, 0.5);
                pad.setYaw(90);
                s.shooter.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 100, 0));
                s.shooter.teleport(pad);
                Fx.p(s.world, Particle.END_ROD, pad.clone().add(0, 1, 0), 40, 0.4, 0.1);
                s.sound(pad, "entity.enderman.teleport", 2f, 0.8f);
                Fx.title(s.shooter, "&b&lSPACE STATION", "&7Welcome aboard.", 50);
            });
        }
    }

    /** What block goes at this offset from the station centre (null = leave it). */
    private static Material pick(int r, int padMid, int dx, int dy, int dz) {
        double d = Math.sqrt(dx * dx + dz * dz);
        // landing pad
        if (dx >= padMid - 4 && dx <= padMid + 4 && Math.abs(dz) <= 4) {
            int px = dx - padMid;
            if (dy == -1) {
                if (Math.abs(px) == 4 || Math.abs(dz) == 4) return (dx + dz) % 2 == 0 ? Material.YELLOW_CONCRETE : Material.BLACK_CONCRETE;
                if ((Math.abs(dz) == 2 && Math.abs(px) <= 2) || (dz == 0 && Math.abs(px) <= 2)) return Material.WHITE_CONCRETE;
                return Material.GRAY_CONCRETE;
            }
            if (dy == -2) return Material.IRON_BLOCK;
            if (dy == 0 && Math.abs(px) == 4 && Math.abs(dz) == 4) return Material.SEA_LANTERN;
            if (dy >= 0 && dy <= 3) return Material.AIR;
            return null;
        }
        // bridge between room and pad
        if (dx > r - 2 && dx < padMid - 4 && Math.abs(dz) <= 1) {
            if (dy == -1) return Material.IRON_BLOCK;
            if (dy >= 0 && dy <= 2) return Material.AIR;
            if (dy == 3) return Material.GLASS;
            return null;
        }
        if (dx > r - 2 && dx < padMid - 4 && Math.abs(dz) == 2 && dy >= -1 && dy <= 3) return Material.GLASS;
        // antenna
        if (dx == 0 && dz == 0 && dy >= 6) return dy == 8 ? Material.LIGHTNING_ROD : Material.IRON_BARS;
        if (d > r + 0.4) return null;
        if (dy == -2) return d <= r - 1 ? Material.POLISHED_ANDESITE : null;
        if (dy == -1) {
            if (d <= 1.5) return Material.SEA_LANTERN;
            if (d > r - 3 && d <= r - 2) return Material.LIGHT_GRAY_CONCRETE;
            if (d > r - 0.6) return Material.POLISHED_ANDESITE;
            return Material.SMOOTH_QUARTZ;
        }
        if (dy >= 0 && dy <= 4) {
            if (d > r - 0.6) {
                if (dx >= r - 1 && Math.abs(dz) <= 1 && dy <= 2) return Material.AIR;
                return dy == 4 ? Material.WHITE_CONCRETE : Material.LIGHT_BLUE_STAINED_GLASS;
            }
            if (dy == 0 && dz == -(r - 2) && Math.abs(dx) <= 1) return dx == -1 ? Material.CHEST : dx == 0 ? Material.CRAFTING_TABLE : Material.FURNACE;
            if (dy == 0 && dz == r - 2 && dx == 0) return Material.ENCHANTING_TABLE;
            return Material.AIR;
        }
        if (dy == 5) return d <= 1 ? Material.SEA_LANTERN : Material.GLASS;
        return null;
    }
}
