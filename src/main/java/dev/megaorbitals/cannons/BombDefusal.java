package dev.megaorbitals.cannons;

import dev.megaorbitals.*;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/** Drops a ticking bomb. Right-click it to open the wire panel and cut the right wire before time runs out! */
public final class BombDefusal extends Cannon {

    private static final String[] WIRE_NAMES = {"&cRed", "&9Blue", "&eYellow", "&aGreen", "&fWhite", "&dPink", "&6Orange"};
    private static final Material[] WIRES = {Material.RED_WOOL, Material.BLUE_WOOL, Material.YELLOW_WOOL, Material.LIME_WOOL,
            Material.WHITE_WOOL, Material.PINK_WOOL, Material.ORANGE_WOOL};

    private final Map<UUID, Bomb> bombs = new HashMap<>();

    private final class Bomb implements InventoryHolder {
        final Strike s;
        final int wires, correct;
        final boolean[] cut;
        final Inventory inv;
        int ticksLeft;
        boolean over;

        Bomb(Strike s, int wires, int seconds) {
            this.s = s;
            this.wires = wires;
            this.correct = ThreadLocalRandom.current().nextInt(wires);
            this.cut = new boolean[wires];
            this.ticksLeft = seconds * 20;
            this.inv = Bukkit.createInventory(this, 27, s.text("&4&lBOMB &8- cut the right wire!"));
            redraw();
        }

        int slot(int wire) { return 13 - wires / 2 + wire; }

        void redraw() {
            for (int k = 0; k < 27; k++) inv.setItem(k, named(Material.BLACK_STAINED_GLASS_PANE, " "));
            for (int w = 0; w < wires; w++) {
                inv.setItem(slot(w), cut[w] ? named(Material.GRAY_DYE, "&8" + WIRE_NAMES[w].substring(2) + " wire (cut)")
                        : named(WIRES[w], WIRE_NAMES[w] + " wire", "&7Click to cut it."));
            }
            inv.setItem(4, named(Material.CLOCK, "&c&l" + Math.max(0, ticksLeft / 20) + "s left"));
        }

        @Override public Inventory getInventory() { return inv; }
    }

    public BombDefusal() {
        super("&4&lBomb Defusal", Material.TNT, "Drops a ticking bomb with a wire panel.", "Right-click it and cut the right wire in time!");
        warning(20, "#E02020");
        option("seconds", 45, "Time on the clock.");
        option("wires", 5, "Number of wires (2 to 7).");
        option("hint-seconds", 10, "Every this many seconds one wrong wire sparks (a hint). 0 = no hints.");
        option("wrong-wire-explodes", false, "Cutting a wrong wire sets it off at once (otherwise it halves the time left).");
        option("power", 8.0, "Explosion power.");
        option("reward", List.of("DIAMOND:3", "EMERALD:6"), "Given to whoever defuses it, MATERIAL:amount.");
    }

    @Override
    public void strike(Strike s) {
        Location l = s.at();
        int wires = Math.max(2, Math.min(7, i("wires")));
        Bomb bomb = new Bomb(s, wires, Math.max(5, i("seconds")));
        BlockDisplay body = Fx.display(l.clone().add(0, 0.5, 0), Material.TNT, 1f);
        TextDisplay label = s.world.spawn(l.clone().add(0, 1.6, 0), TextDisplay.class, t -> {
            t.setBillboard(Display.Billboard.CENTER);
            t.text(s.text("&c&l" + bomb.ticksLeft / 20));
        });
        Interaction hit = s.world.spawn(l, Interaction.class, it -> {
            it.setInteractionWidth(1.2f);
            it.setInteractionHeight(1.2f);
            it.setResponsive(true);
        });
        s.tag(body); s.tag(label); s.tag(hit);
        bombs.put(hit.getUniqueId(), bomb);
        for (Player p : s.world.getPlayers()) if (p.getLocation().distance(l) < 40) Fx.title(p, "&4&lBOMB!", "&7Right-click it and cut the right wire!", 50);
        int hint = i("hint-seconds") * 20;
        s.loop(1, t -> {
            if (bomb.over) { cleanup(bomb, body, label, hit); return false; }
            bomb.ticksLeft--;
            if (bomb.ticksLeft % 20 == 0) {
                int left = bomb.ticksLeft / 20;
                label.text(s.text((left <= 10 ? "&4&l" : "&c&l") + left));
                bomb.redraw();
                s.sound(l, "block.note_block.hat", 2f, left <= 10 ? 2f : 1.2f);
            }
            if (hint > 0 && t > 0 && t % hint == 0) {
                List<Integer> wrong = new ArrayList<>();
                for (int w = 0; w < wires; w++) if (!bomb.cut[w] && w != bomb.correct) wrong.add(w);
                if (!wrong.isEmpty()) {
                    int w = wrong.get(ThreadLocalRandom.current().nextInt(wrong.size()));
                    bomb.cut[w] = true;
                    bomb.redraw();
                    Fx.p(s.world, Particle.ELECTRIC_SPARK, l.clone().add(0, 1, 0), 20, 0.3, 0.1);
                    for (Player p : s.world.getPlayers()) if (p.getLocation().distance(l) < 30)
                        s.actionBar(p, "&eThe " + WIRE_NAMES[w] + " &ewire sparks and burns out - it wasn't that one.");
                }
            }
            Fx.p(s.world, Particle.SMOKE, l.clone().add(0, 1.1, 0), 1, 0.05, 0.01);
            if (bomb.ticksLeft <= 0) {
                bomb.over = true;
                cleanup(bomb, body, label, hit);
                s.explode(l, (float) d("power"), false);
                return false;
            }
            return true;
        });
    }

    private void cleanup(Bomb bomb, Entity... es) {
        for (HumanEntity h : new ArrayList<>(bomb.inv.getViewers())) h.closeInventory();
        for (Entity e : es) {
            bombs.remove(e.getUniqueId());
            if (e.isValid()) e.remove();
        }
    }

    @EventHandler
    public void onClickBomb(PlayerInteractEntityEvent e) {
        Bomb b = bombs.get(e.getRightClicked().getUniqueId());
        if (b == null || b.over) return;
        e.setCancelled(true);
        e.getPlayer().openInventory(b.inv);
        e.getPlayer().playSound(e.getPlayer().getLocation(), "block.iron_trapdoor.open", 1f, 0.8f);
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        if (e.getInventory().getHolder(false) instanceof Bomb) e.setCancelled(true);
    }

    @EventHandler
    public void onCut(InventoryClickEvent e) {
        if (!(e.getInventory().getHolder(false) instanceof Bomb b)) return;
        e.setCancelled(true);
        if (b.over || !(e.getWhoClicked() instanceof Player p) || e.getClickedInventory() != b.inv) return;
        int wire = -1;
        for (int w = 0; w < b.wires; w++) if (b.slot(w) == e.getRawSlot()) wire = w;
        if (wire < 0 || b.cut[wire]) return;
        b.cut[wire] = true;
        Strike s = b.s;
        p.playSound(p.getLocation(), "entity.sheep.shear", 1f, 1f);
        if (wire == b.correct) {
            b.over = true;
            Location l = s.at();
            Fx.p(s.world, Particle.TOTEM_OF_UNDYING, l.clone().add(0, 1, 0), 80, 0.6, 0.4);
            s.sound(l, "ui.toast.challenge_complete", 4f, 1f);
            for (Player o : s.world.getPlayers()) if (o.getLocation().distance(l) < 50)
                Fx.title(o, "&a&lDEFUSED!", "&7by " + p.getName() + " with " + b.ticksLeft / 20 + "s to spare", 60);
            s.give(p, list("reward"));
            return;
        }
        if (b("wrong-wire-explodes")) {
            b.ticksLeft = 1;
            p.sendActionBar(s.text("&4&lWRONG WIRE!"));
        } else {
            b.ticksLeft = Math.max(20, b.ticksLeft / 2);
            p.sendActionBar(s.text("&c&lWRONG WIRE! &7The timer speeds up..."));
            p.playSound(p.getLocation(), "block.note_block.bass", 1f, 0.5f);
        }
        b.redraw();
    }

    private static ItemStack named(Material m, String name, String... lore) {
        ItemStack it = new ItemStack(m);
        ItemMeta meta = it.getItemMeta();
        meta.displayName(MegaOrbitals.color(name).decoration(TextDecoration.ITALIC, false));
        if (lore.length > 0) {
            List<net.kyori.adventure.text.Component> l = new ArrayList<>();
            for (String s : lore) l.add(MegaOrbitals.color(s).decoration(TextDecoration.ITALIC, false));
            meta.lore(l);
        }
        it.setItemMeta(meta);
        return it;
    }
}
