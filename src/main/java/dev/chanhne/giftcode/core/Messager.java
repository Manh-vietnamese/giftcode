package dev.chanhne.giftcode.core;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class Messager {

    private YamlConfiguration messagesConfig;
    private final JavaPlugin plugin;
    private final File messagesFile;

    public Messager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        reload();
    }

    // Reload lại file messages.yml
    public void reload() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }

        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public Component get(Player player, String key, String... placeholders) {
        String message = messagesConfig.getString(key);
        if (message == null) Component.text(key);
        message = replacePlaceholders(player, message, placeholders);
        return LegacyComponentSerializer.legacyAmpersand().deserialize(message).decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false);
    }

    public List<Component> getList(Player player, String key, String... placeholders) {
        List<Component> list = new ArrayList<>();
        for (String line : messagesConfig.getStringList(key)) {
            line = replacePlaceholders(player, line, placeholders);
            list.add(LegacyComponentSerializer.legacyAmpersand().deserialize(line).decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false));
        }

        return list;
    }

    public void send(CommandSender sender, String key, String... placeholders) {
        Player player = sender instanceof Player ? (Player) sender : null;
        sender.sendMessage(get(player, key, placeholders));
    }

    private String replacePlaceholders(Player player, String text, String... placeholders) {
        if (text == null) return "";

        for (int i = 0; i + 1 < placeholders.length; i += 2) {
            text = text.replace("%" + placeholders[i] + "%", placeholders[i + 1]);
        }

        if (player != null && Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            text = PlaceholderAPI.setPlaceholders(player, text);
        }

        return text;
    }
}