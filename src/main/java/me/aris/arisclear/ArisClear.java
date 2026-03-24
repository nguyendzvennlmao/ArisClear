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
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArisClear extends JavaPlugin implements CommandExecutor, TabCompleter {

    private int timeLeft;
    private final String L = "QVJJU19ORVRXT1JLX1NUT1JF";

    @Override
    public void onEnable() {
        saveDefaultConfig();
        if (!checkLicense()) {
            Bukkit.getConsoleSender().sendMessage(color("&#ff0812&lᴀʀɪs ɴᴇᴛᴡᴏʀᴋ &8» &fLicense không hợp lệ! Plugin sẽ bị tắt."));
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        Bukkit.getConsoleSender().sendMessage(color("&#facc15&lᴀʀɪs ɴᴇᴛᴡᴏʀᴋ &8» &aLicense kích hoạt thành công."));
        getCommand("arisclear").setExecutor(this);
        getCommand("arisclear").setTabCompleter(this);
        timeLeft = getConfig().getInt("settings.clear-interval");
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) new ArisPlaceholder(this).register();
        startTimer();
    }

    private boolean checkLicense() {
        String key = getConfig().getString("license-key");
        String dec = new String(Base64.getDecoder().decode(L));
        return key != null && key.equals(dec);
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
        if (a.length == 1 && s.hasPermission("arisclear.admin")) sug.add("clear");
        return sug;
    }

    private void checkAndNotify(int t) {
        String p = getConfig().getString("messages.prefix");
        boolean chat = getConfig().getBoolean("settings.display.chat");
        boolean bar = getConfig().getBoolean("settings.display.actionbar");
        if (t == 60) {
            broadcast(color(p + getConfig().getString("messages.warning-1p")), chat, bar);
            playSound(getConfig().getString("settings.sound-1p"));
        } else if (t <= 5 && t >= 1) {
            broadcast(color(p + getConfig().getString("messages.warning-5s").replace("%time%", String.valueOf(t))), chat, bar);
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

    public String color(String m) {
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
