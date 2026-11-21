package dev.megaorbitals;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class OrbitalCommands implements TabExecutor {

    private static final List<String> SUBS = List.of("list", "reload", "info", "search", "menu", "undo", "history", "satellites");
    private static final List<String> SPECIAL_ITEMS = List.of("binoculars", "satellite-item", "core:common", "core:rare", "core:epic", "core:legendary");
    private final MegaOrbitals plugin;

    public OrbitalCommands(MegaOrbitals plugin) {
        this.plugin = plugin;
    }

    private void send(CommandSender s, String legacy) {
        s.sendMessage(MegaOrbitals.color(plugin.getConfig().getString("messages.prefix", "") + legacy));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        return cmd.getName().equalsIgnoreCase("orbital") ? orbital(sender, args) : give(sender, args);
    }

    private boolean orbital(CommandSender sender, String[] args) {
        Settings st = plugin.settings();
        if (args.length == 0) {
            send(sender, "&cUsage: /orbital <cannon|list [page]|search <word>|menu|info <cannon>|reload>");
            return true;
        }
        String a = args[0].toLowerCase();
        Cannon c = plugin.cannon(a);
        if (c == null && a.equals("list")) {
            List<String> ids = new ArrayList<>(plugin.cannons().keySet());
            int per = 40, pages = Math.max(1, (ids.size() + per - 1) / per);
            int page = 1;
            if (args.length > 1) try { page = Integer.parseInt(args[1]); } catch (NumberFormatException ignored) {}
            page = Math.max(1, Math.min(pages, page));
            send(sender, "&6" + ids.size() + " cannons &8(page " + page + "/" + pages + ") &7- /orbital list <page>, /orbital search <word>, /orbital menu");
            sender.sendMessage(MegaOrbitals.color("&f" + String.join("&7, &f", ids.subList((page - 1) * per, Math.min(ids.size(), page * per)))));
            return true;
        }
        if (c == null && a.equals("search")) {
            String q = args.length > 1 ? String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length)).toLowerCase() : "";
            List<String> hits = new ArrayList<>();
            for (Cannon x : plugin.cannons().values()) {
                String hay = (x.id() + " " + x.name() + " " + String.join(" ", x.lore())).toLowerCase().replaceAll("&.", "");
                if (hay.contains(q)) hits.add(x.id());
            }
            send(sender, "&6" + hits.size() + " match \"" + q + "\": &f" + String.join("&7, &f", hits));
            return true;
        }
        if (c == null && a.equals("undo")) {
            if (!sender.hasPermission("megaorbitals.undo")) { send(sender, st.msg("no-permission")); return true; }
            Rollback rb = plugin.rollback();
            if (args.length > 1 && args[1].equalsIgnoreCase("all")) {
                int n = 0;
                for (Rollback.Record r : new ArrayList<>(rb.records())) if (!r.restored() && r.size() > 0) { rb.restore(r); n++; }
                send(sender, n == 0 ? st.msg("undo-none") : st.msg("undo-all").replace("{count}", String.valueOf(n)));
                return true;
            }
            java.util.UUID who = null;
            if (args.length > 1) {
                var op = Bukkit.getOfflinePlayerIfCached(args[1]);
                if (op == null) { send(sender, st.msg("player-not-found").replace("{player}", args[1])); return true; }
                who = op.getUniqueId();
            } else if (sender instanceof Player p && !p.hasPermission("megaorbitals.undo.others")) {
                who = p.getUniqueId();
            }
            Rollback.Record r = rb.latest(who);
            if (r == null) { send(sender, st.msg("undo-none")); return true; }
            Cannon rc = plugin.cannon(r.cannon);
            send(sender, st.msg("undo-done").replace("{cannon}", rc == null ? r.cannon : rc.name())
                    .replace("{player}", r.shooterName).replace("{blocks}", String.valueOf(r.size())));
            if (r.truncated()) send(sender, st.msg("undo-truncated"));
            rb.restore(r);
            return true;
        }
        if (c == null && a.equals("history")) {
            if (!sender.hasPermission("megaorbitals.undo")) { send(sender, st.msg("no-permission")); return true; }
            send(sender, st.msg("history-header"));
            long now = System.currentTimeMillis();
            List<Rollback.Record> recs = plugin.rollback().records();
            int shown = 0;
            for (int k = recs.size() - 1; k >= 0 && shown < 10; k--) {
                Rollback.Record r = recs.get(k);
                if (r.restored() || r.size() == 0) continue;
                sender.sendMessage(MegaOrbitals.color(st.msg("history-line").replace("{id}", String.valueOf(r.id)).replace("{cannon}", r.cannon)
                        .replace("{player}", r.shooterName).replace("{blocks}", String.valueOf(r.size()))
                        .replace("{minutes}", String.valueOf((now - r.time) / 60000))));
                shown++;
            }
            if (shown == 0) sender.sendMessage(MegaOrbitals.color(st.msg("undo-none")));
            return true;
        }
        if (c == null && a.equals("satellites")) {
            if (sender instanceof Player p) plugin.satellites().list(p);
            else send(sender, st.msg("players-only"));
            return true;
        }
        if (c == null && a.equals("menu")) {
            if (sender instanceof Player p) plugin.menu().open(p, 0);
            else send(sender, st.msg("players-only"));
            return true;
        }
        if (c == null && a.equals("reload")) {
            plugin.loadCannons();
            plugin.lang().reload();
            plugin.progression().registerRecipes();
            send(sender, st.msg("reloaded").replace("{count}", String.valueOf(plugin.cannons().size())));
            return true;
        }
        if (c == null && a.equals("info")) {
            Cannon ic = args.length > 1 ? plugin.cannon(args[1]) : null;
            if (ic == null) {
                send(sender, st.msg("unknown-cannon").replace("{cannon}", args.length > 1 ? args[1] : "?"));
                return true;
            }
            send(sender, ic.name() + " &8(" + ic.id() + ")");
            for (String l : ic.lore()) sender.sendMessage(MegaOrbitals.color("  &7" + l));
            sender.sendMessage(MegaOrbitals.color("  &8warning: &f" + ic.warningTicks() / 20.0 + "s  &8breaks blocks: &f" + ic.breaksBlocks()
                    + (st.disabled(ic) ? "  &cDISABLED" : "")));
            sender.sendMessage(MegaOrbitals.color("  &8settings: &fconfigs/" + ic.id() + ".yml"));
            return true;
        }
        if (c == null) {
            send(sender, st.msg("unknown-cannon").replace("{cannon}", args[0]));
            return true;
        }
        if (!(sender instanceof Player p)) {
            send(sender, "&cOnly players can fire an orbital (it targets the block you look at).");
            return true;
        }
        plugin.fire(c, p, false);
        return true;
    }

    private boolean give(CommandSender sender, String[] args) {
        Settings st = plugin.settings();
        if (args.length == 0) {
            send(sender, "&cUsage: /orbitalgive <cannon|all> [player] [amount]");
            return true;
        }
        Player target;
        if (args.length >= 2) {
            target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                send(sender, st.msg("player-not-found").replace("{player}", args[1]));
                return true;
            }
        } else if (sender instanceof Player p) {
            target = p;
        } else {
            send(sender, "&cSpecify a player.");
            return true;
        }
        int amount = 1;
        if (args.length >= 3) {
            try { amount = Math.max(1, Math.min(64, Integer.parseInt(args[2]))); } catch (NumberFormatException ignored) {}
        }

        String special = args[0].toLowerCase();
        ItemStack extra = special.equals("binoculars") ? plugin.spotter().binoculars()
                : special.equals("satellite-item") ? plugin.satellites().satelliteItem()
                : special.startsWith("core:") && Catalog.TIERS.contains(special.substring(5)) ? plugin.progression().core(special.substring(5), 1)
                : null;
        if (extra != null) {
            extra.setAmount(amount);
            target.getInventory().addItem(extra).values().forEach(left -> target.getWorld().dropItem(target.getLocation(), left));
            send(sender, st.msg("given").replace("{player}", target.getName()).replace("{amount}", String.valueOf(amount)).replace("{cannon}", special));
            return true;
        }

        List<Cannon> list = new ArrayList<>();
        if (args[0].equalsIgnoreCase("all") && plugin.cannon("all") == null) {
            for (Cannon c : plugin.cannons().values()) if (!st.disabled(c)) list.add(c);
        } else {
            Cannon c = plugin.cannon(args[0]);
            if (c == null) {
                send(sender, st.msg("unknown-cannon").replace("{cannon}", args[0]));
                return true;
            }
            if (st.disabled(c)) {
                send(sender, st.msg("disabled-cannon"));
                return true;
            }
            list.add(c);
        }
        for (Cannon c : list) {
            ItemStack stack = plugin.createItem(c, amount);
            target.getInventory().addItem(stack).values()
                    .forEach(left -> target.getWorld().dropItem(target.getLocation(), left));
        }
        send(sender, st.msg("given").replace("{player}", target.getName()).replace("{amount}", String.valueOf(amount))
                .replace("{cannon}", list.size() == 1 ? list.get(0).name() : list.size() + " cannons"));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> out = new ArrayList<>();
        boolean orbital = cmd.getName().equalsIgnoreCase("orbital");
        if (args.length == 1) {
            List<String> opts = new ArrayList<>(plugin.cannons().keySet());
            if (orbital) opts.addAll(SUBS); else { opts.add("all"); opts.addAll(SPECIAL_ITEMS); }
            String pre = args[0].toLowerCase();
            for (String s : opts) if (s.startsWith(pre)) out.add(s);
        } else if (args.length == 2 && orbital && args[0].equalsIgnoreCase("info")) {
            String pre = args[1].toLowerCase();
            for (String s : plugin.cannons().keySet()) if (s.startsWith(pre)) out.add(s);
        } else if (args.length == 2 && !orbital) {
            String pre = args[1].toLowerCase();
            for (Player p : Bukkit.getOnlinePlayers()) if (p.getName().toLowerCase().startsWith(pre)) out.add(p.getName());
        }
        return out;
    }
}
