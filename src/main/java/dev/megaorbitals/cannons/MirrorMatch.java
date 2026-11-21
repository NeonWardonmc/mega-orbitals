package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;
import java.util.*;

/** Every player nearby must fight a mirror copy of themselves that copies their every move, swing for swing. */
public final class MirrorMatch extends Cannon {

    private static final class Match {
        final Strike s;
        final Player player;
        final Mannequin copy;
        final Location centre;
        int tick, lastSwing = -100;
        boolean over;
        Match(Strike s, Player player, Mannequin copy, Location centre) {
            this.s = s; this.player = player; this.copy = copy; this.centre = centre;
        }
    }

    private final Map<UUID, Match> byPlayer = new HashMap<>();
    private final Map<UUID, Match> byCopy = new HashMap<>();

    public MirrorMatch() {
        super("&f&lMirror Match", Material.GLASS_PANE, "Every player nearby fights a mirror copy", "of themselves that copies their every move.");
        warning(30, "#D0E0FF");
        noBlockDamage();
        option("radius", 25.0, "Players this close to the target get a mirror.");
        option("seconds", 60, "How long the fight lasts.");
        option("copy-health", 20.0, "Health of each mirror copy.");
        option("copy-damage", 3.0, "Damage the copy deals when it swings back and connects.");
        option("reach", 3.2, "How close the copy must be for its swing to hit.");
        option("gap", 6.0, "Starting distance between a player and their mirror.");
        option("reward", List.of("EXPERIENCE_BOTTLE:8"), "Given for shattering your mirror, MATERIAL:amount.");
    }

    @Override
    public void strike(Strike s) {
        for (Player p : s.players(s.at(), d("radius"))) {
            if (byPlayer.containsKey(p.getUniqueId())) continue;
            Vector look = p.getLocation().getDirection().setY(0);
            if (look.lengthSquared() < 0.01) look = new Vector(1, 0, 0);
            look.normalize();
            Location centre = p.getLocation().add(look.multiply(d("gap") / 2));
            Location spawn = mirror(p.getLocation(), centre);
            Mannequin copy = s.world.spawn(spawn, Mannequin.class, m -> {
                s.tag(m);
                m.setProfile(ResolvableProfile.resolvableProfile(p.getPlayerProfile()));
                m.customName(s.text("&f" + p.getName() + " &7(mirror)"));
                m.setCustomNameVisible(true);
                m.setImmovable(true);
                var hp = m.getAttribute(Attribute.MAX_HEALTH);
                if (hp != null) { hp.setBaseValue(d("copy-health")); m.setHealth(d("copy-health")); }
                PlayerInventory inv = p.getInventory();
                EntityEquipment eq = m.getEquipment();
                eq.setHelmet(clone(inv.getHelmet()));
                eq.setChestplate(clone(inv.getChestplate()));
                eq.setLeggings(clone(inv.getLeggings()));
                eq.setBoots(clone(inv.getBoots()));
                eq.setItemInMainHand(clone(inv.getItemInMainHand()));
                eq.setItemInOffHand(clone(inv.getItemInOffHand()));
            });
            Match mt = new Match(s, p, copy, centre);
            byPlayer.put(p.getUniqueId(), mt);
            byCopy.put(copy.getUniqueId(), mt);
            Fx.p(s.world, Particle.END_ROD, spawn.clone().add(0, 1, 0), 40, 0.4, 0.05);
            Fx.title(p, "&f&lMIRROR MATCH", "&7Defeat yourself. It copies everything you do.", 60);
            s.sound(p.getLocation(), "block.glass.break", 2f, 0.6f);
            run(mt);
        }
    }

    private void run(Match mt) {
        Strike s = mt.s;
        int life = i("seconds") * 20;
        s.loop(1, t -> {
            mt.tick = t;
            Player p = mt.player;
            if (mt.over) return false;
            if (!mt.copy.isValid() || mt.copy.isDead()) {
                end(mt);
                if (p.isOnline()) {
                    Fx.title(p, "&a&lSHATTERED!", "&7You beat your own reflection.", 50);
                    s.give(p, list("reward"));
                }
                return false;
            }
            if (!p.isOnline() || p.isDead() || p.getWorld() != mt.copy.getWorld() || t >= life) {
                if (p.isOnline() && !p.isDead() && t >= life) Fx.title(p, "&7&lTIME", "&7Your reflection fades away...", 40);
                end(mt);
                return false;
            }
            Location pl = p.getLocation();
            Location m = mirror(pl, mt.centre);
            Double pg = ground(pl), mg = ground(m);
            m.setY(mg == null ? pl.getY() : mg + (pg == null ? 0 : Math.max(0, pl.getY() - pg)));
            m.setYaw(pl.getYaw() + 180f);
            m.setPitch(pl.getPitch());
            mt.copy.teleport(m);
            mt.copy.setPose(p.isSneaking() ? Pose.SNEAKING : Pose.STANDING, true);
            if (t % 5 == 0) Fx.dust(s.world, m.clone().add(0, 1, 0), Color.fromRGB(0xD0E0FF), 0.8f, 2, 0.3);
            return true;
        });
    }

    private void end(Match mt) {
        mt.over = true;
        byPlayer.remove(mt.player.getUniqueId());
        byCopy.remove(mt.copy.getUniqueId());
        if (mt.copy.isValid()) {
            Fx.p(mt.copy.getWorld(), Particle.BLOCK, mt.copy.getLocation().add(0, 1, 0), 40, 0.3, 0);
            mt.copy.getWorld().playSound(mt.copy.getLocation(), "block.glass.break", 1.5f, 1.2f);
            mt.copy.remove();
        }
    }

    /** Point-reflects l through c (horizontally). */
    private static Location mirror(Location l, Location c) {
        Location m = l.clone();
        m.setX(2 * c.getX() - l.getX());
        m.setZ(2 * c.getZ() - l.getZ());
        return m;
    }

    private static Double ground(Location l) {
        World w = l.getWorld();
        int x = l.getBlockX(), z = l.getBlockZ();
        if (!Blocks.loaded(w, x, z)) return null;
        for (int y = l.getBlockY() + 2; y >= l.getBlockY() - 8; y--) {
            Block b = w.getBlockAt(x, y, z);
            if (b.getType().isSolid() && !b.getRelative(0, 1, 0).getType().isSolid() && !b.getRelative(0, 2, 0).getType().isSolid()) return (double) y + 1;
        }
        return null;
    }

    private static ItemStack clone(ItemStack it) { return it == null ? null : it.clone(); }

    @EventHandler
    public void onSwing(PlayerAnimationEvent e) {
        Match mt = byPlayer.get(e.getPlayer().getUniqueId());
        if (mt == null || mt.over || !mt.copy.isValid()) return;
        mt.copy.swingMainHand();
        if (mt.tick - mt.lastSwing < 10) return;
        mt.lastSwing = mt.tick;
        Player p = e.getPlayer();
        if (p.getLocation().distance(mt.copy.getLocation()) > d("reach")) return;
        mt.s.damage(p, d("copy-damage"));
        p.setVelocity(Fx.dir(mt.copy.getLocation(), p.getLocation()).multiply(0.4).setY(0.25));
        p.getWorld().playSound(p.getLocation(), "entity.player.attack.strong", 1f, 1.3f);
    }

    @EventHandler
    public void onCopyDeath(EntityDeathEvent e) {
        if (!byCopy.containsKey(e.getEntity().getUniqueId())) return;
        e.getDrops().clear();
        e.setDroppedExp(0);
    }
}
