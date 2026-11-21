package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/** A floating pinata appears. Whack it enough times and it bursts into a shower of loot. */
public final class LootPinata extends Cannon {

    private static final class Pinata {
        final Strike s;
        final int needed;
        int hits;
        boolean burst;
        Pinata(Strike s, int needed) { this.s = s; this.needed = needed; }
    }

    private final Map<UUID, Pinata> pinatas = new HashMap<>();

    public LootPinata() {
        super("&d&lLoot Pinata", Material.CAKE, "A floating pinata appears. Hit it enough", "times and it bursts into loot!");
        warning(15, "#FF60C0");
        noBlockDamage();
        option("hits", 25, "Hits needed to burst it.");
        option("seconds", 180, "It floats away if nobody bursts it in time.");
        option("loot", List.of("DIAMOND:4", "EMERALD:8", "GOLD_INGOT:12", "COOKIE:32", "CAKE:1", "GOLDEN_APPLE:2", "EXPERIENCE_BOTTLE:12",
                "FIREWORK_ROCKET:16", "NAME_TAG:1", "ENCHANTED_GOLDEN_APPLE:1"), "Loot that pours out, MATERIAL:amount.");
        option("loot-rolls", 6, "How many entries from the loot list are dropped.");
    }

    @Override
    public void strike(Strike s) {
        Location home = s.at(0, 2.5, 0);
        Llama llama = s.mob(home, Llama.class, l -> {
            l.setAI(false);
            l.setGravity(false);
            l.setSilent(true);
            l.setPersistent(true);
            l.setColor(Llama.Color.values()[ThreadLocalRandom.current().nextInt(Llama.Color.values().length)]);
            l.customName(s.text("&d&lP&e&lI&b&lÑ&a&lA&6&lT&c&lA"));
            l.setCustomNameVisible(true);
            l.getInventory().setDecor(new ItemStack(Material.MAGENTA_CARPET));
        });
        Pinata p = new Pinata(s, Math.max(1, i("hits")));
        pinatas.put(llama.getUniqueId(), p);
        s.sound(home, "entity.llama.ambient", 3f, 1.5f);
        for (Player pl : s.world.getPlayers()) if (pl.getLocation().distance(home) < 40)
            Fx.title(pl, "&d&lPIÑATA!", "&7Hit it " + p.needed + " times!", 40);
        int life = i("seconds") * 20;
        s.loop(1, t -> {
            if (p.burst || !llama.isValid()) { pinatas.remove(llama.getUniqueId()); return false; }
            if (t >= life) {
                Fx.p(s.world, Particle.CLOUD, llama.getLocation(), 30, 0.5, 0.05);
                pinatas.remove(llama.getUniqueId());
                llama.remove();
                return false;
            }
            Location l = home.clone().add(Math.sin(t * 0.05) * 0.5, Math.sin(t * 0.08) * 0.4, Math.cos(t * 0.05) * 0.5);
            l.setYaw(t * 3f);
            llama.teleport(l);
            if (t % 10 == 0) Fx.p(s.world, Particle.NOTE, l.clone().add(0, 2.3, 0), 1, 0.3, 0);
            return true;
        });
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onHit(EntityDamageEvent e) {
        Pinata p = pinatas.get(e.getEntity().getUniqueId());
        if (p == null) return;
        e.setCancelled(true);
        if (p.burst || !(e instanceof EntityDamageByEntityEvent ev)) return;
        Entity dmg = ev.getDamager();
        Player hitter = dmg instanceof Player pl ? pl : dmg instanceof Projectile pr && pr.getShooter() instanceof Player pl2 ? pl2 : null;
        if (hitter == null) return;
        Strike s = p.s;
        LivingEntity le = (LivingEntity) e.getEntity();
        p.hits++;
        le.playHurtAnimation(0);
        Location l = le.getLocation().add(0, 1, 0);
        Fx.p(s.world, Particle.HAPPY_VILLAGER, l, 6, 0.4, 0);
        Fx.dust(s.world, l, Color.fromRGB(ThreadLocalRandom.current().nextInt(0xFFFFFF)), 1.5f, 6, 0.5);
        s.sound(l, "entity.llama.hurt", 1.5f, 0.8f + p.hits / (float) p.needed);
        s.actionBar(hitter, "&dPiñata: &f" + p.hits + "&7/" + p.needed);
        if (p.hits < p.needed) return;
        p.burst = true;
        pinatas.remove(le.getUniqueId());
        burst(s, le, hitter);
    }

    private void burst(Strike s, LivingEntity le, Player hitter) {
        Location l = le.getLocation().add(0, 1, 0);
        le.remove();
        Fx.p(s.world, Particle.FIREWORK, l, 120, 0.6, 0.35);
        Fx.p(s.world, Particle.EXPLOSION, l, 2, 0.2, 0);
        s.sound(l, "entity.firework_rocket.twinkle", 4f, 1f);
        s.sound(l, "entity.player.levelup", 3f, 1.3f);
        List<ItemStack> loot = Strike.items(list("loot"));
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        for (int k = 0; k < i("loot-rolls") && !loot.isEmpty(); k++) {
            ItemStack it = loot.get(rnd.nextInt(loot.size())).clone();
            Item drop = s.world.dropItem(l, it);
            drop.setVelocity(new Vector(rnd.nextDouble(-0.4, 0.4), rnd.nextDouble(0.3, 0.7), rnd.nextDouble(-0.4, 0.4)));
        }
        for (Player p : s.world.getPlayers()) if (p.getLocation().distance(l) < 40)
            Fx.title(p, "&d&lPIÑATA BURST!", "&7by " + hitter.getName(), 40);
    }
}
