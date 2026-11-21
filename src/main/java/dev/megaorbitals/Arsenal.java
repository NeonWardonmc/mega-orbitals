package dev.megaorbitals;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiConsumer;

/**
 * The building blocks of the 1000 "arsenal" cannons (1.5.0): every arsenal cannon is one
 * {@link Payload} (what happens where it hits) delivered with one {@link Pattern} (where and when it hits).
 * See {@link Combo}.
 *
 * Nothing in the enum constants touches Bukkit registries until a strike actually runs,
 * so the build-time config exporter can create these cannons without a server.
 */
public final class Arsenal {

    private Arsenal() {}

    /** What one impact does. p = strength (1 = normal). */
    public interface Hit { void hit(Strike s, Location l, double p); }

    // ------------------------------------------------------------ helpers used by payloads

    static ThreadLocalRandom rnd() { return ThreadLocalRandom.current(); }

    static void pot(LivingEntity e, PotionEffectType t, int ticks, int amp) {
        e.addPotionEffect(new PotionEffect(t, ticks, amp));
    }

    /** Players near l, the shooter included (for helpful payloads). */
    static List<Player> friends(Location l, double r) {
        List<Player> out = new ArrayList<>();
        for (Player p : l.getWorld().getPlayers()) if (!p.isDead() && p.getLocation().distance(l) <= r) out.add(p);
        return out;
    }

    static Location jitter(Location l, double r) {
        return l.clone().add(rnd().nextDouble(-r, r), 0, rnd().nextDouble(-r, r));
    }

    static void hurt(Strike s, Location l, double r, double dmg) {
        for (LivingEntity v : s.victims(l, r)) s.damage(v, dmg);
    }

    static void push(Strike s, Location l, double r, double force, double up) {
        for (LivingEntity v : s.victims(l, r)) {
            Vector d = Fx.dir(l, v.getLocation());
            v.setVelocity(v.getVelocity().add(d.multiply(force)).setY(up));
        }
    }

    /** The top solid block under l (or null). */
    static Block top(Location l) {
        Block b = Blocks.surface(l).getBlock().getRelative(0, -1, 0);
        return b.getType().isSolid() ? b : null;
    }

    // ------------------------------------------------------------ payloads

    public enum Payload {
        BLAST("Blast", "&c", "#FF4020", Material.TNT, "explosives", 2, true, "Explosions", (s, l, p) -> {
            s.explode(l, (float) (2.5 * p), false);
        }),
        INFERNO("Inferno", "&6", "#FF8000", Material.BLAZE_POWDER, "weather", 2, true, "Fiery blasts that set everything alight", (s, l, p) -> {
            s.explode(l, (float) (1.2 * p), true);
            Fx.p(s.world, Particle.FLAME, l, 40, 1.2 * p, 0.05);
            for (LivingEntity v : s.victims(l, 3.5 * p)) v.setFireTicks(120);
        }),
        THUNDER("Thunder", "&e", "#FFFF60", Material.LIGHTNING_ROD, "weather", 2, false, "Lightning bolts", (s, l, p) -> {
            s.world.strikeLightningEffect(l);
            hurt(s, l, 2.5 * p, 6 * p);
            for (LivingEntity v : s.victims(l, 2.5 * p)) v.setFireTicks(60);
        }),
        FROST("Frost", "&b", "#A0E0FF", Material.BLUE_ICE, "weather", 1, false, "Freezing bursts that slow and chill", (s, l, p) -> {
            Fx.p(s.world, Particle.SNOWFLAKE, l.clone().add(0, 1, 0), 60, 1.5 * p, 0.05);
            s.sound(l, "block.glass.break", 1.5f, 0.6f);
            for (LivingEntity v : s.victims(l, 3 * p)) {
                v.setFreezeTicks(Math.max(v.getFreezeTicks(), 200));
                pot(v, PotionEffectType.SLOWNESS, 100, 2);
                s.damage(v, 2 * p);
            }
        }),
        VENOM("Venom", "&2", "#40A020", Material.SPIDER_EYE, "curses", 1, false, "Poison clouds", (s, l, p) -> {
            Fx.dust(s.world, l.clone().add(0, 1, 0), Color.fromRGB(0x50C030), 2f, 40, 1.5 * p);
            for (LivingEntity v : s.victims(l, 3 * p)) { pot(v, PotionEffectType.POISON, 140, 1); s.damage(v, p); }
        }),
        WITHER("Withering", "&8", "#303030", Material.WITHER_ROSE, "curses", 2, false, "Withering decay", (s, l, p) -> {
            Fx.p(s.world, Particle.SOUL, l.clone().add(0, 1, 0), 20, 1.2 * p, 0.03);
            Fx.dust(s.world, l.clone().add(0, 1, 0), Color.BLACK, 2f, 30, 1.2 * p);
            for (LivingEntity v : s.victims(l, 3 * p)) { pot(v, PotionEffectType.WITHER, 120, 1); s.damage(v, 2 * p); }
        }),
        GRAVITY("Gravity", "&5", "#6020A0", Material.OBSIDIAN, "space", 2, false, "Gravity wells that drag everything in", (s, l, p) -> {
            Fx.p(s.world, Particle.REVERSE_PORTAL, l.clone().add(0, 1, 0), 50, 2 * p, 0.1);
            s.sound(l, "block.respawn_anchor.deplete", 1.5f, 0.5f);
            for (LivingEntity v : s.victims(l, 6 * p)) v.setVelocity(Fx.dir(v.getLocation(), l).multiply(0.9 * p).setY(0.25));
            s.later(10, () -> hurt(s, l, 1.8 * p, 4 * p));
        }),
        SHOCKWAVE("Shockwave", "&f", "#FFFFFF", Material.PISTON, "control", 1, false, "Shockwaves that hurl people away", (s, l, p) -> {
            Fx.p(s.world, Particle.EXPLOSION, l.clone().add(0, 0.5, 0), 3, p, 0);
            s.sound(l, "entity.generic.explode", 1.5f, 1.4f);
            hurt(s, l, 4 * p, 3 * p);
            push(s, l, 4.5 * p, 1.4 * p, 0.55);
        }),
        UPDRAFT("Updraft", "&f", "#E0F0FF", Material.FEATHER, "control", 1, false, "Updrafts that make people float", (s, l, p) -> {
            Fx.p(s.world, Particle.CLOUD, l, 30, 1.2 * p, 0.05);
            for (LivingEntity v : s.victims(l, 3.5 * p)) pot(v, PotionEffectType.LEVITATION, (int) (50 * p), 1);
        }),
        ROCKET("Rocket", "&c", "#FF6060", Material.FIREWORK_ROCKET, "control", 1, false, "Blasts that launch people sky-high", (s, l, p) -> {
            Fx.p(s.world, Particle.FIREWORK, l, 40, 0.8, 0.15);
            s.sound(l, "entity.firework_rocket.launch", 1.5f, 0.7f);
            for (LivingEntity v : s.victims(l, 3 * p)) v.setVelocity(new Vector(0, 1.4 * Math.min(2, p), 0));
        }),
        SHADOW("Shadow", "&0", "#202020", Material.INK_SAC, "curses", 1, false, "Shadows that blind and darken", (s, l, p) -> {
            Fx.p(s.world, Particle.SQUID_INK, l.clone().add(0, 1, 0), 60, 1.5 * p, 0.05);
            for (LivingEntity v : s.victims(l, 4 * p)) { pot(v, PotionEffectType.BLINDNESS, 100, 0); pot(v, PotionEffectType.DARKNESS, 140, 0); }
        }),
        VERTIGO("Vertigo", "&d", "#C060C0", Material.FERMENTED_SPIDER_EYE, "curses", 0, false, "Dizzy spells", (s, l, p) -> {
            Fx.p(s.world, Particle.WITCH, l.clone().add(0, 1, 0), 30, 1.2 * p, 0.05);
            for (LivingEntity v : s.victims(l, 4 * p)) { pot(v, PotionEffectType.NAUSEA, 200, 0); pot(v, PotionEffectType.SLOWNESS, 80, 0); }
        }),
        WEB("Web", "&7", "#D0D0D0", Material.COBWEB, "control", 1, true, "Sticky cobweb traps", (s, l, p) -> {
            for (LivingEntity v : s.victims(l, 3 * p)) pot(v, PotionEffectType.SLOWNESS, 100, 3);
            Fx.p(s.world, Particle.ITEM_SNOWBALL, l, 20, p, 0.05);
            if (!s.breaks()) return;
            List<Block> webs = new ArrayList<>();
            int r = (int) Math.max(1, Math.round(p));
            for (int x = -r; x <= r; x++) for (int z = -r; z <= r; z++) for (int y = 0; y <= 1; y++) {
                Block b = l.clone().add(x, y, z).getBlock();
                if (b.getType().isAir() && rnd().nextInt(3) > 0) { Blocks.set(b, Material.COBWEB); webs.add(b); }
            }
            s.later(300, () -> { for (Block b : webs) if (b.getType() == Material.COBWEB) Blocks.put(b, Material.AIR, false); });
        }),
        ANVIL("Anvil", "&7", "#808080", Material.ANVIL, "weapons", 1, false, "Falling anvils", (s, l, p) -> {
            FallingBlock fb = s.fallingBlock(l.clone().add(0, 18, 0), Material.ANVIL, true);
            fb.setHurtEntities(true);
            fb.setDamagePerBlock((float) (1.5 * p));
            fb.setMaxDamage(40);
        }),
        ARROW("Arrow", "&f", "#C0A080", Material.ARROW, "weapons", 1, false, "Volleys of arrows from above", (s, l, p) -> {
            for (int k = 0; k < 8; k++) {
                Arrow a = s.world.spawnArrow(jitter(l, 2).add(0, 16, 0), new Vector(0, -1, 0), 2.2f, 6f);
                a.setDamage(3 * p);
                a.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
                if (s.shooter != null) a.setShooter(s.shooter);
                s.expire(s.tag(a), 100);
            }
        }),
        FANG("Fang", "&8", "#606060", Material.EMERALD, "weapons", 1, false, "Snapping evoker fangs", (s, l, p) -> {
            List<Location> spots = new ArrayList<>(Fx.circle(l, 1.6, 8));
            spots.add(l);
            for (Location f : spots) {
                s.world.spawn(Blocks.surface(f), EvokerFangs.class, e -> { if (s.shooter != null) e.setOwner(s.shooter); s.tag(e); });
            }
        }),
        SONIC("Sonic", "&3", "#20A0A0", Material.ECHO_SHARD, "beams", 2, false, "Sonic booms that ignore cover", (s, l, p) -> {
            Fx.p(s.world, Particle.SONIC_BOOM, l.clone().add(0, 1, 0), 1, 0, 0);
            s.sound(l, "entity.warden.sonic_boom", 1.5f, 1f);
            hurt(s, l, 2.5 * p, 5 * p);
        }),
        VOLT("Volt", "&e", "#F0F040", Material.COPPER_BLOCK, "beams", 1, false, "Crackling electric shocks", (s, l, p) -> {
            Fx.p(s.world, Particle.ELECTRIC_SPARK, l.clone().add(0, 1, 0), 60, 1.3 * p, 0.3);
            s.sound(l, "entity.bee.hurt", 1f, 0.5f);
            for (LivingEntity v : s.victims(l, 3 * p)) {
                s.damage(v, 4 * p);
                v.setVelocity(new Vector(rnd().nextDouble(-0.3, 0.3), 0.3, rnd().nextDouble(-0.3, 0.3)));
            }
        }),
        FIREWORK("Firework", "&d", "#FF60FF", Material.FIREWORK_STAR, "fun", 0, false, "Colourful firework bursts", (s, l, p) -> {
            Location c = l.clone().add(0, 1.5, 0);
            Fx.p(s.world, Particle.FIREWORK, c, 60, 0.3, 0.25);
            for (int k = 0; k < 3; k++) Fx.dust(s.world, c, Color.fromRGB(rnd().nextInt(0xFFFFFF)), 2f, 20, 1.2);
            s.sound(c, "entity.firework_rocket.blast", 2f, 1f);
            s.sound(c, "entity.firework_rocket.twinkle", 2f, 1f);
            hurt(s, l, 2.5 * p, 3 * p);
        }),
        MENDING("Mending", "&a", "#60FF80", Material.GOLDEN_APPLE, "buffs", 1, false, "Healing light for everyone it touches", (s, l, p) -> {
            Fx.p(s.world, Particle.HEART, l.clone().add(0, 1.5, 0), 8, 1.2, 0);
            for (Player v : friends(l, 4 * p)) {
                var max = v.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH);
                v.setHealth(Math.min(max == null ? 20 : max.getValue(), v.getHealth() + 4 * p));
                pot(v, PotionEffectType.REGENERATION, 100, 1);
            }
        }),
        RUSH("Rush", "&b", "#60FFFF", Material.SUGAR, "buffs", 0, false, "Bursts of speed and haste", (s, l, p) -> {
            Fx.dust(s.world, l.clone().add(0, 1, 0), Color.AQUA, 1.5f, 30, 1.5);
            for (Player v : friends(l, 4 * p)) { pot(v, PotionEffectType.SPEED, 300, 1); pot(v, PotionEffectType.HASTE, 300, 1); }
        }),
        BULWARK("Bulwark", "&9", "#4060FF", Material.SHIELD, "buffs", 1, false, "Protective barriers", (s, l, p) -> {
            Fx.dustSphere(s.world, l.clone().add(0, 1, 0), 2, Color.BLUE, 1.2f, 40);
            for (Player v : friends(l, 4 * p)) { pot(v, PotionEffectType.RESISTANCE, 300, 1); pot(v, PotionEffectType.ABSORPTION, 600, 1); }
        }),
        BOUNCE("Bounce", "&a", "#80FF60", Material.SLIME_BALL, "fun", 0, false, "Bouncy slime blasts", (s, l, p) -> {
            Fx.p(s.world, Particle.ITEM_SLIME, l.clone().add(0, 0.5, 0), 40, 1.2 * p, 0.1);
            s.sound(l, "entity.slime.jump", 1.5f, 0.8f);
            for (LivingEntity v : s.victims(l, 3 * p)) { v.setVelocity(v.getVelocity().setY(1.1)); pot(v, PotionEffectType.JUMP_BOOST, 200, 2); }
        }),
        TIDAL("Tidal", "&9", "#3060FF", Material.WATER_BUCKET, "weather", 1, false, "Crashing water splashes", (s, l, p) -> {
            Fx.p(s.world, Particle.SPLASH, l.clone().add(0, 0.5, 0), 120, 1.5 * p, 0.3);
            Fx.p(s.world, Particle.BUBBLE_POP, l.clone().add(0, 0.5, 0), 40, 1.5 * p, 0.05);
            s.sound(l, "entity.generic.splash", 1.5f, 0.6f);
            for (LivingEntity v : s.victims(l, 3.5 * p)) v.setFireTicks(0);
            hurt(s, l, 3.5 * p, p);
            push(s, l, 3.5 * p, p, 0.35);
        }),
        MAGMA("Magma", "&6", "#FF6000", Material.MAGMA_BLOCK, "terrain", 1, true, "Patches of scorching magma", (s, l, p) -> {
            Fx.p(s.world, Particle.LAVA, l, 15, p, 0);
            hurt(s, l, 2.5 * p, 2 * p);
            if (!s.breaks()) return;
            int r = (int) Math.max(1, Math.round(1.5 * p));
            for (int x = -r; x <= r; x++) for (int z = -r; z <= r; z++) {
                if (x * x + z * z > r * r) continue;
                Block b = top(l.clone().add(x, 0, z));
                if (b != null) Blocks.set(b, Material.MAGMA_BLOCK);
            }
        }),
        CRYSTAL("Crystal", "&d", "#C080FF", Material.AMETHYST_SHARD, "weapons", 1, false, "Shattering crystal shards", (s, l, p) -> {
            Fx.block(s.world, Particle.BLOCK, l.clone().add(0, 1, 0), Material.AMETHYST_BLOCK, 50, 1.2 * p);
            s.sound(l, "block.amethyst_cluster.break", 2f, 0.7f);
            hurt(s, l, 2.5 * p, 4 * p);
        }),
        DUNE("Dune", "&e", "#E0C060", Material.SAND, "terrain", 0, true, "Avalanches of falling sand", (s, l, p) -> {
            Fx.block(s.world, Particle.FALLING_DUST, l.clone().add(0, 2, 0), Material.SAND, 40, 1.5);
            for (int k = 0; k < (int) Math.max(2, 4 * p); k++) s.fallingBlock(jitter(l, 1.2).add(0, 10 + k, 0), Material.SAND, !s.breaks());
            s.later(20, () -> settle(s, l, p));
        }),
        BOULDER("Boulder", "&7", "#909090", Material.COBBLESTONE, "weapons", 1, false, "Crushing boulders from orbit", (s, l, p) -> {
            FallingBlock fb = s.fallingBlock(l.clone().add(0, 22, 0), Material.COBBLESTONE, true);
            fb.setHurtEntities(true);
            fb.setDamagePerBlock((float) (1.2 * p));
            fb.setMaxDamage(30);
            s.later(Strike.fallTicks(22), () -> Fx.block(s.world, Particle.BLOCK, l, Material.COBBLESTONE, 40, 1));
        }),
        WISDOM("Wisdom", "&a", "#80FF40", Material.EXPERIENCE_BOTTLE, "fun", 0, false, "Showers of experience", (s, l, p) -> {
            for (int k = 0; k < 3; k++) s.world.spawn(l.clone().add(0, 1, 0), ExperienceOrb.class, o -> o.setExperience((int) Math.max(1, 5 * p)));
            Fx.p(s.world, Particle.HAPPY_VILLAGER, l.clone().add(0, 1, 0), 10, 0.8, 0);
        }),
        GOLDEN("Golden", "&6", "#FFD040", Material.GOLD_INGOT, "fun", 0, false, "Rains of gold nuggets", (s, l, p) -> {
            Item it = s.world.dropItem(l.clone().add(0, 3, 0), new org.bukkit.inventory.ItemStack(Material.GOLD_NUGGET, (int) Math.max(1, 3 * p)));
            it.setVelocity(new Vector(rnd().nextDouble(-0.2, 0.2), 0.2, rnd().nextDouble(-0.2, 0.2)));
            s.sound(l, "entity.experience_orb.pickup", 1f, 1.6f);
        }),
        BLOOM("Bloom", "&d", "#FF80C0", Material.POPPY, "fun", 0, true, "Blooms of flowers and gentle healing", (s, l, p) -> {
            Fx.p(s.world, Particle.CHERRY_LEAVES, l.clone().add(0, 1.5, 0), 30, 1.5, 0.05);
            for (Player v : friends(l, 3 * p)) v.setHealth(Math.min(v.getHealth() + 2, Objects.requireNonNull(v.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH)).getValue()));
            if (!s.breaks()) return;
            Material[] flowers = {Material.POPPY, Material.DANDELION, Material.CORNFLOWER, Material.ALLIUM, Material.OXEYE_DAISY, Material.AZURE_BLUET};
            for (int k = 0; k < 6; k++) {
                Block b = top(jitter(l, 2.5 * p));
                if (b == null || (b.getType() != Material.GRASS_BLOCK && b.getType() != Material.DIRT)) continue;
                Block above = b.getRelative(0, 1, 0);
                if (above.getType().isAir()) Blocks.set(above, flowers[rnd().nextInt(flowers.length)]);
            }
        }),
        GALE("Gale", "&f", "#E0FFF0", Material.WIND_CHARGE, "weather", 1, false, "Gusts of wind that scatter everyone", (s, l, p) -> {
            Fx.p(s.world, Particle.GUST, l.clone().add(0, 1, 0), 4, 1, 0);
            s.sound(l, "entity.wind_charge.wind_burst", 2f, 0.8f);
            push(s, l, 5 * p, 1.8 * p, 0.6);
        }),
        UNDEAD("Undead", "&2", "#408040", Material.ZOMBIE_HEAD, "mobs", 1, false, "Zombies that climb out of the ground", (s, l, p) -> {
            Zombie z = s.mob(l, Zombie.class, zz -> { zz.setShouldBurnInDay(false); zz.setAdult(); });
            Fx.block(s.world, Particle.BLOCK, l, Material.DIRT, 20, 0.5);
            s.hunt(z, 30);
            s.expire(z, 20 * 120);
        }),
        CREEPER("Creeper", "&a", "#40C040", Material.CREEPER_HEAD, "mobs", 2, true, "Lit creepers dropped from above", (s, l, p) -> {
            Creeper c = s.mob(l.clone().add(0, 6, 0), Creeper.class, cc -> { cc.setMaxFuseTicks(40); cc.setExplosionRadius((int) Math.max(1, Math.round(3 * p))); });
            c.ignite();
        }),
        HIVE("Hive", "&e", "#FFC020", Material.HONEYCOMB, "mobs", 1, false, "Swarms of angry bees", (s, l, p) -> {
            for (int k = 0; k < 2; k++) {
                Bee b = s.mob(l.clone().add(0, 1.5, 0), Bee.class, bb -> bb.setAnger(600));
                s.hunt(b, 30);
                s.expire(b, 20 * 60);
            }
        }),
        VEX("Vex", "&7", "#B0C0D0", Material.IRON_SWORD, "mobs", 2, false, "Vexes that hunt the nearest player", (s, l, p) -> {
            Vex v = s.mob(l.clone().add(0, 2, 0), Vex.class, null);
            s.hunt(v, 40);
            s.expire(v, 20 * 30);
        }),
        SHULKER("Shulker", "&5", "#A060A0", Material.SHULKER_SHELL, "weapons", 1, false, "Homing shulker bullets", (s, l, p) -> {
            LivingEntity target = s.nearest(l, 25);
            if (target == null) { Fx.p(s.world, Particle.END_ROD, l, 10, 0.5, 0.05); return; }
            ShulkerBullet b = s.world.spawn(l.clone().add(0, 5, 0), ShulkerBullet.class, sb -> {
                sb.setTarget(target);
                if (s.shooter != null) sb.setShooter(s.shooter);
                s.tag(sb);
            });
            s.expire(b, 200);
        }),
        DRAGONFIRE("Dragonfire", "&5", "#C020FF", Material.DRAGON_BREATH, "weapons", 3, false, "Lingering clouds of dragon's breath", (s, l, p) -> {
            s.every(10, 10, t -> {
                Fx.p(s.world, Particle.DRAGON_BREATH, l.clone().add(0, 0.5, 0), 30, 1.5 * p, 0.02);
                if (t % 2 == 0) hurt(s, l, 2.5 * p, 2 * p);
            });
        }),
        WARP("Warp", "&5", "#8000FF", Material.CHORUS_FRUIT, "control", 1, false, "Warps that teleport people somewhere random", (s, l, p) -> {
            Fx.p(s.world, Particle.PORTAL, l.clone().add(0, 1, 0), 60, 1.2, 0.5);
            for (LivingEntity v : s.victims(l, 3 * p)) {
                Location to = Blocks.surface(jitter(v.getLocation(), 10 * p));
                to.setYaw(v.getLocation().getYaw());
                to.setPitch(v.getLocation().getPitch());
                v.teleport(to);
                s.sound(to, "item.chorus_fruit.teleport", 1f, 1f);
            }
        }),
        BOMBLET("Bomblet", "&4", "#C02020", Material.FIRE_CHARGE, "explosives", 3, true, "Lit TNT dropped from the sky", (s, l, p) -> {
            s.tnt(l.clone().add(0, 8, 0), (float) (2.5 * p), 40);
        });

        public final String word, colour, hex, category, lore;
        public final Material icon;
        public final int weight;
        public final boolean breaks;
        public final Hit hit;

        Payload(String word, String colour, String hex, Material icon, String category, int weight, boolean breaks, String lore, Hit hit) {
            this.word = word; this.colour = colour; this.hex = hex; this.icon = icon; this.category = category;
            this.weight = weight; this.breaks = breaks; this.lore = lore; this.hit = hit;
        }
    }

    /** Sand settling: a small damage pulse a second after the sand lands. */
    private static void settle(Strike s, Location l, double p) {
        hurt(s, l, 2 * p, 2 * p);
        for (LivingEntity v : s.victims(l, 2 * p)) pot(v, PotionEffectType.SLOWNESS, 60, 1);
    }

    // ------------------------------------------------------------ patterns

    /** Shooter's horizontal look direction (or east). */
    static Vector look(Strike s) {
        Vector v = s.shooter != null ? s.shooter.getLocation().getDirection().setY(0) : new Vector(1, 0, 0);
        return v.lengthSquared() < 0.01 ? new Vector(1, 0, 0) : v.normalize();
    }

    static Location ground(Strike s, Location l) { return s.surface(l); }

    public interface Runner { void run(Strike s, Combo c, BiConsumer<Location, Double> hit); }

    public enum Pattern {
        STRIKE("Strike", 0, 40, 0, 1, 0, "in one massive hit", (s, c, hit) -> hit.accept(s.at(), 2.5)),
        RAIN("Shower", 1, 30, 12, 20, 3, "raining down all over the area", (s, c, hit) ->
                s.every(c.i("interval-ticks"), Math.max(1, c.i("count")), t -> hit.accept(s.randomPoint(c.d("radius")), 1.0))),
        RING("Ring", 1, 30, 8, 12, 0, "in a ring around the target", (s, c, hit) -> {
            for (Location l : Fx.circle(s.at(), c.d("radius"), Math.max(3, c.i("count")))) hit.accept(ground(s, l), 1.0);
        }),
        NOVA("Nova", 2, 30, 14, 4, 8, "in rings that spread outward", (s, c, hit) -> {
            int n = Math.max(1, c.i("count"));
            s.every(c.i("interval-ticks"), n, t -> {
                double r = c.d("radius") * (t + 1) / n;
                for (Location l : Fx.circle(s.at(), r, (int) Math.max(6, r * 1.2))) hit.accept(ground(s, l), 0.8);
            });
        }),
        IMPLOSION("Implosion", 2, 30, 14, 4, 8, "in rings that close in on the centre", (s, c, hit) -> {
            int n = Math.max(1, c.i("count"));
            s.every(c.i("interval-ticks"), n, t -> {
                double r = c.d("radius") * (n - t) / n;
                for (Location l : Fx.circle(s.at(), r, (int) Math.max(6, r * 1.2))) hit.accept(ground(s, l), 0.8);
                if (t == n - 1) hit.accept(s.at(), 2.0);
            });
        }),
        LANCE("Lance", 1, 30, 30, 12, 2, "in a straight line where you aim", (s, c, hit) -> {
            Vector d = look(s);
            int n = Math.max(2, c.i("count"));
            double len = c.d("radius");
            s.every(c.i("interval-ticks"), n, t -> hit.accept(ground(s, s.at().add(d.clone().multiply(-len / 2 + len * t / (n - 1)))), 1.0));
        }),
        CROSS("Cross", 1, 30, 10, 5, 3, "in a giant cross", (s, c, hit) -> {
            int n = Math.max(1, c.i("count"));
            hit.accept(s.at(), 1.2);
            s.every(c.i("interval-ticks"), n, t -> {
                double r = c.d("radius") * (t + 1) / n;
                hit.accept(ground(s, s.at(r, 0, 0)), 1.0);
                hit.accept(ground(s, s.at(-r, 0, 0)), 1.0);
                hit.accept(ground(s, s.at(0, 0, r)), 1.0);
                hit.accept(ground(s, s.at(0, 0, -r)), 1.0);
            });
        }),
        SPIRAL("Spiral", 2, 30, 12, 24, 2, "spiralling out from the centre", (s, c, hit) -> {
            int n = Math.max(2, c.i("count"));
            double start = rnd().nextDouble(Math.PI * 2);
            s.every(c.i("interval-ticks"), n, t -> {
                double a = start + t * 0.6, r = c.d("radius") * t / (n - 1);
                hit.accept(ground(s, s.at(Math.cos(a) * r, 0, Math.sin(a) * r)), 1.0);
            });
        }),
        GRID("Grid", 2, 30, 10, 5, 4, "in a grid that sweeps the area row by row", (s, c, hit) -> {
            int n = Math.max(2, c.i("count"));
            double r = c.d("radius");
            s.every(c.i("interval-ticks"), n, row -> {
                for (int col = 0; col < n; col++)
                    hit.accept(ground(s, s.at(-r + 2 * r * row / (n - 1), 0, -r + 2 * r * col / (n - 1))), 0.9);
            });
        }),
        BARRAGE("Barrage", 3, 40, 10, 40, 1, "in a relentless rapid-fire barrage", (s, c, hit) ->
                s.every(c.i("interval-ticks"), Math.max(1, c.i("count")), t -> hit.accept(s.randomPoint(c.d("radius")), 1.0))),
        CHAIN("Chain", 2, 20, 20, 8, 6, "jumping from target to target", (s, c, hit) -> {
            Set<UUID> done = new HashSet<>();
            Location[] cur = {s.at()};
            hit.accept(s.at(), 1.0);
            s.loop(Math.max(1, c.i("interval-ticks")), t -> {
                if (t >= c.i("count")) return false;
                LivingEntity next = null;
                double best = Double.MAX_VALUE;
                for (LivingEntity v : s.victims(cur[0], c.d("radius"))) {
                    double dd = v.getLocation().distanceSquared(cur[0]);
                    if (!done.contains(v.getUniqueId()) && dd < best) { best = dd; next = v; }
                }
                if (next == null) return false;
                done.add(next.getUniqueId());
                Fx.dustLine(s.world, cur[0].clone().add(0, 1, 0), next.getLocation().add(0, 1, 0), c.warningColor(), 1.2f, 0.4);
                cur[0] = next.getLocation();
                hit.accept(next.getLocation(), 1.0);
                return true;
            });
        }),
        SEEKER("Seeker", 2, 20, 30, 10, 4, "on every target in range, one after another", (s, c, hit) -> {
            List<LivingEntity> vs = s.victims(s.at(), c.d("radius"));
            vs.sort(Comparator.comparingDouble(v -> v.getLocation().distanceSquared(s.at())));
            int n = Math.min(vs.size(), Math.max(1, c.i("count")));
            if (n == 0) { hit.accept(s.at(), 1.0); return; }
            s.every(c.i("interval-ticks"), n, t -> { if (vs.get(t).isValid()) hit.accept(vs.get(t).getLocation(), 1.0); });
        }),
        CARPET("Carpet", 3, 40, 16, 6, 5, "carpet-bombing forward in rows", (s, c, hit) -> {
            Vector d = look(s), side = new Vector(-d.getZ(), 0, d.getX());
            int n = Math.max(2, c.i("count"));
            double len = c.d("radius");
            s.every(c.i("interval-ticks"), n, t -> {
                Location row = s.at().add(d.clone().multiply(-len / 2 + len * t / (n - 1)));
                for (int k = -2; k <= 2; k++) hit.accept(ground(s, row.clone().add(side.clone().multiply(k * len / 8))), 0.9);
            });
        }),
        STAR("Star", 2, 30, 10, 5, 2, "in the shape of a five-pointed star", (s, c, hit) -> {
            List<Location> pts = new ArrayList<>();
            for (int k = 0; k < 5; k++) {
                double a = -Math.PI / 2 + k * 2 * Math.PI / 5;
                pts.add(s.at(Math.cos(a) * c.d("radius"), 0, Math.sin(a) * c.d("radius")));
            }
            int per = Math.max(1, c.i("count"));
            s.every(c.i("interval-ticks"), 5 * per, t -> {
                int edge = t / per, step = t % per;
                Location a = pts.get((edge * 2) % 5), b = pts.get((edge * 2 + 2) % 5);
                Vector v = b.toVector().subtract(a.toVector()).multiply((double) step / per);
                hit.accept(ground(s, a.clone().add(v)), 0.9);
            });
        }),
        ORBIT("Orbit", 1, 20, 6, 24, 3, "circling the target over and over", (s, c, hit) -> {
            double start = rnd().nextDouble(Math.PI * 2);
            s.every(c.i("interval-ticks"), Math.max(1, c.i("count")), t -> {
                double a = start + t * Math.PI / 6;
                hit.accept(ground(s, s.at(Math.cos(a) * c.d("radius"), 0, Math.sin(a) * c.d("radius"))), 0.9);
            });
        }),
        PILLAR("Pillar", 1, 30, 18, 6, 4, "stacked in a column falling from the sky", (s, c, hit) -> {
            int n = Math.max(1, c.i("count"));
            s.every(c.i("interval-ticks"), n, t -> hit.accept(s.at(0, c.d("radius") * (n - 1 - t) / Math.max(1, n - 1), 0), 1.0));
        }),
        CLUSTER("Cluster", 1, 30, 6, 6, 15, "as one hit that splits into many", (s, c, hit) -> {
            hit.accept(s.at(), 1.5);
            s.later(c.i("interval-ticks"), () -> {
                for (Location l : Fx.circle(s.at(), c.d("radius"), Math.max(1, c.i("count")))) hit.accept(ground(s, l), 1.0);
            });
        }),
        MINES("Mines", 1, 10, 12, 8, 1200, "as hidden mines that go off when someone walks by", (s, c, hit) -> {
            List<Location> mines = new ArrayList<>();
            for (int k = 0; k < Math.max(1, c.i("count")); k++) mines.add(s.randomPoint(c.d("radius")));
            int life = Math.max(20, c.i("interval-ticks"));
            s.loop(5, t -> {
                if (t * 5 >= life || mines.isEmpty()) return false;
                for (Iterator<Location> it = mines.iterator(); it.hasNext(); ) {
                    Location m = it.next();
                    if (t % 4 == 0) Fx.dust(s.world, m.clone().add(0, 0.1, 0), c.warningColor(), 0.6f, 1, 0.1);
                    if (!s.victims(m, 1.8).isEmpty()) { it.remove(); s.sound(m, "block.stone_pressure_plate.click_on", 2f, 0.6f); hit.accept(m, 1.2); }
                }
                return true;
            });
        }),
        PULSE("Pulse", 1, 30, 0, 6, 12, "pulsing on the same spot again and again", (s, c, hit) ->
                s.every(c.i("interval-ticks"), Math.max(1, c.i("count")), t -> hit.accept(s.at(), 1.2))),
        ZIGZAG("Zigzag", 1, 30, 24, 12, 2, "zigzagging forward where you aim", (s, c, hit) -> {
            Vector d = look(s), side = new Vector(-d.getZ(), 0, d.getX());
            int n = Math.max(2, c.i("count"));
            double len = c.d("radius");
            s.every(c.i("interval-ticks"), n, t -> {
                Location l = s.at().add(d.clone().multiply(-len / 2 + len * t / (n - 1))).add(side.clone().multiply((t % 2 == 0 ? 1 : -1) * len / 6));
                hit.accept(ground(s, l), 1.0);
            });
        }),
        COMET("Comet", 2, 30, 20, 16, 2, "streaking across the area like a comet", (s, c, hit) -> {
            double a = rnd().nextDouble(Math.PI * 2);
            Vector d = new Vector(Math.cos(a), 0, Math.sin(a));
            int n = Math.max(2, c.i("count"));
            double len = c.d("radius");
            s.every(c.i("interval-ticks"), n, t -> {
                Location l = ground(s, s.at().add(d.clone().multiply(-len / 2 + len * t / (n - 1))));
                Fx.p(s.world, Particle.FLAME, l.clone().add(0, 1, 0), 10, 0.3, 0.02);
                hit.accept(l, t == n - 1 ? 2.0 : 0.6);
            });
        }),
        HALO("Halo", 2, 30, 10, 16, 15, "in two rings, inner then outer", (s, c, hit) -> {
            int n = Math.max(3, c.i("count"));
            for (Location l : Fx.circle(s.at(), c.d("radius") / 2, n / 2)) hit.accept(ground(s, l), 1.0);
            s.later(c.i("interval-ticks"), () -> { for (Location l : Fx.circle(s.at(), c.d("radius"), n)) hit.accept(ground(s, l), 1.0); });
        }),
        FAN("Fan", 1, 30, 18, 6, 3, "fanning out in a cone where you aim", (s, c, hit) -> {
            Vector d = look(s);
            Location from = s.shooter != null && s.shooter.getWorld() == s.world && s.shooter.getLocation().distance(s.at()) < 60
                    ? s.shooter.getLocation() : s.at().subtract(d.clone().multiply(c.d("radius") / 2));
            int n = Math.max(1, c.i("count"));
            s.every(c.i("interval-ticks"), n, t -> {
                double r = c.d("radius") * (t + 1) / n;
                for (int k = -2; k <= 2; k++) {
                    double ang = Math.toRadians(k * 20);
                    Vector v = d.clone().rotateAroundY(ang).multiply(r);
                    hit.accept(ground(s, from.clone().add(v)), 0.9);
                }
            });
        }),
        VOLLEY("Volley", 2, 30, 25, 8, 0, "on everyone in range at the same moment", (s, c, hit) -> {
            List<LivingEntity> vs = s.victims(s.at(), c.d("radius"));
            Collections.shuffle(vs);
            int n = Math.max(1, c.i("count"));
            for (int k = 0; k < n; k++) hit.accept(k < vs.size() ? vs.get(k).getLocation() : s.randomPoint(c.d("radius") / 2), 1.0);
        }),
        DELTA("Delta", 1, 30, 10, 6, 2, "tracing a giant triangle", (s, c, hit) -> {
            List<Location> pts = new ArrayList<>();
            for (int k = 0; k < 3; k++) {
                double a = -Math.PI / 2 + k * 2 * Math.PI / 3;
                pts.add(s.at(Math.cos(a) * c.d("radius"), 0, Math.sin(a) * c.d("radius")));
            }
            int per = Math.max(1, c.i("count"));
            s.every(c.i("interval-ticks"), 3 * per, t -> {
                int edge = t / per, step = t % per;
                Location a = pts.get(edge), b = pts.get((edge + 1) % 3);
                hit.accept(ground(s, a.clone().add(b.toVector().subtract(a.toVector()).multiply((double) step / per))), 1.0);
            });
        });

        public final String word, lore;
        public final int weight, warning, count, interval;
        public final double radius;
        public final Runner runner;

        Pattern(String word, int weight, int warning, double radius, int count, int interval, String lore, Runner runner) {
            this.word = word; this.weight = weight; this.warning = warning; this.radius = radius;
            this.count = count; this.interval = interval; this.lore = lore; this.runner = runner;
        }
    }
}
