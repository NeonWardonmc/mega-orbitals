package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/** Minigame: survive round after round of tougher mobs. Everyone still in the arena gets rewards after each wave. */
public final class WaveSurvival extends Cannon {
    public WaveSurvival() {
        super("&c&lWave Survival", Material.ZOMBIE_HEAD, "Minigame: rounds of harder and harder mobs.", "Survivors get rewards after each wave.");
        warning(30, "#C03030");
        noBlockDamage();
        option("waves", 5, "Number of waves.");
        option("arena-radius", 20.0, "Players this close to the target are in the game.");
        option("base-mobs", 4, "Mobs in the first wave.");
        option("extra-mobs-per-wave", 2, "Extra mobs added each wave.");
        option("wave-time-limit", 120, "Seconds before a wave ends even if mobs are left (they despawn).");
        option("break-seconds", 8, "Rest time between waves.");
        option("reward", List.of("BREAD:4", "IRON_INGOT:3", "ARROW:16"), "Given to each player after every wave, MATERIAL:amount.");
        option("final-reward", List.of("DIAMOND:5", "GOLDEN_APPLE:2"), "Extra prize for surviving the last wave.");
    }

    @Override
    public void strike(Strike s) {
        Location c = s.at();
        double r = d("arena-radius");
        int waves = Math.max(1, i("waves"));
        BossBar bar = BossBar.bossBar(s.text("&c&lWAVE SURVIVAL"), 1f, BossBar.Color.RED, BossBar.Overlay.NOTCHED_10);
        List<Mob> alive = new ArrayList<>();
        int[] wave = {0}, timer = {i("break-seconds") * 20}, total = {0};
        boolean[] fighting = {false};
        Set<Player> shown = new HashSet<>();
        s.loop(1, t -> {
            List<Player> in = inArena(s, c, r);
            if (t % 10 == 0) {
                Fx.dustRing(s.world, c.clone().add(0, 0.2, 0), r, Color.RED, 1.5f);
                for (Player p : s.world.getPlayers()) {
                    boolean near = p.getLocation().distance(c) <= r + 15;
                    if (near && shown.add(p)) p.showBossBar(bar);
                    else if (!near && shown.remove(p)) p.hideBossBar(bar);
                }
            }
            if (in.isEmpty() && t > 200) {
                finish(s, bar, shown, alive, "&c&lGAME OVER", "&7Nobody is left in the arena.");
                return false;
            }
            alive.removeIf(m -> !m.isValid() || m.isDead());
            if (fighting[0]) {
                timer[0]--;
                if (t % 40 == 0) for (Mob m : alive) if (m.getTarget() == null || !m.getTarget().isValid()) s.hunt(m, r + 20);
                bar.name(s.text("&c&lWAVE " + wave[0] + "/" + waves + " &8» &f" + alive.size() + " mobs left &8| &e" + Math.max(0, timer[0] / 20) + "s"));
                bar.progress(total[0] == 0 ? 0 : Math.min(1f, alive.size() / (float) total[0]));
                if (!alive.isEmpty() && timer[0] > 0) return true;
                for (Mob m : alive) { Fx.p(s.world, Particle.POOF, m.getLocation(), 10, 0.3, 0.02); m.remove(); }
                alive.clear();
                fighting[0] = false;
                for (Player p : in) {
                    s.give(p, list("reward"));
                    Fx.title(p, "&a&lWAVE " + wave[0] + " CLEARED", wave[0] < waves ? "&7Rewards handed out. Get ready..." : "", 40);
                }
                s.sound(c, "ui.toast.challenge_complete", 4f, 1f);
                if (wave[0] >= waves) {
                    for (Player p : in) s.give(p, list("final-reward"));
                    finish(s, bar, shown, alive, "&6&lVICTORY!", "&7You survived all " + waves + " waves.");
                    return false;
                }
                timer[0] = i("break-seconds") * 20;
                return true;
            }
            timer[0]--;
            bar.name(s.text("&a&lNEXT WAVE IN &f" + Math.max(0, timer[0] / 20 + 1) + "s"));
            bar.progress(1f);
            if (timer[0] > 0) return true;
            wave[0]++;
            int count = i("base-mobs") + (wave[0] - 1) * i("extra-mobs-per-wave");
            for (int k = 0; k < count; k++) alive.add(spawn(s, c, r, wave[0]));
            total[0] = alive.size();
            timer[0] = i("wave-time-limit") * 20;
            fighting[0] = true;
            for (Player p : in) Fx.title(p, "&c&lWAVE " + wave[0], "&7" + count + " mobs incoming!", 40);
            s.sound(c, "event.raid.horn", 6f, 1f);
            return true;
        });
    }

    private List<Player> inArena(Strike s, Location c, double r) {
        List<Player> out = new ArrayList<>();
        for (Player p : s.world.getPlayers())
            if (!p.isDead() && p.getGameMode() != GameMode.SPECTATOR && p.getLocation().distance(c) <= r) out.add(p);
        return out;
    }

    private Mob spawn(Strike s, Location c, double r, int wave) {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        List<Class<? extends Mob>> pool = new ArrayList<>(List.of(Zombie.class, Skeleton.class, Spider.class));
        if (wave >= 2) pool.addAll(List.of(Husk.class, Stray.class, CaveSpider.class));
        if (wave >= 3) pool.addAll(List.of(Witch.class, Vindicator.class, Pillager.class));
        if (wave >= 4) pool.addAll(List.of(WitherSkeleton.class, Blaze.class, Evoker.class));
        if (wave >= 6) pool.addAll(List.of(Ravager.class, PiglinBrute.class));
        Class<? extends Mob> type = pool.get(rnd.nextInt(pool.size()));
        double a = rnd.nextDouble(Math.PI * 2);
        Location l = s.surface(c.clone().add(Math.cos(a) * r * 0.8, 0, Math.sin(a) * r * 0.8));
        Mob m = s.mob(l, type, mm -> {
            mm.setRemoveWhenFarAway(false);
            if (mm instanceof Zombie z) z.setShouldBurnInDay(false);
            if (mm instanceof AbstractSkeleton sk) sk.setShouldBurnInDay(false);
            if (mm instanceof PiglinAbstract pa) pa.setImmuneToZombification(true);
            if (wave >= 3 && mm.getEquipment() != null && rnd.nextInt(3) == 0) {
                mm.getEquipment().setHelmet(new ItemStack(wave >= 5 ? Material.DIAMOND_HELMET : Material.IRON_HELMET));
                mm.getEquipment().setHelmetDropChance(0);
            }
            var hp = mm.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH);
            if (hp != null && wave > 1) { hp.setBaseValue(hp.getBaseValue() * (1 + 0.15 * (wave - 1))); mm.setHealth(hp.getBaseValue()); }
        });
        Fx.p(s.world, Particle.LARGE_SMOKE, l.clone().add(0, 1, 0), 15, 0.4, 0.02);
        s.hunt(m, r + 20);
        return m;
    }

    private void finish(Strike s, BossBar bar, Set<Player> shown, List<Mob> alive, String title, String sub) {
        for (Player p : shown) { p.hideBossBar(bar); Fx.title(p, title, sub, 60); }
        for (Mob m : alive) if (m.isValid()) m.remove();
    }
}
