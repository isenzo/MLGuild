package me.isenzo.mlguilds.message;

import lombok.RequiredArgsConstructor;
import me.isenzo.mlguilds.Main;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Locale;

@RequiredArgsConstructor
public class MessageManager {
    private final Main plugin;
    private FileConfiguration messageConfig;

    public MessageManager(Main plugin, Locale locale) {
        this.plugin = plugin;
        loadLanguageFile(locale);
    }

    public void loadLanguageFile(Locale locale) {
        File languageFile = new File(plugin.getDataFolder() + "/messages", "messages_" + locale.getLanguage() + ".yml");
        if (!languageFile.exists()) {
            plugin.saveResource("messages/messages_" + locale.getLanguage() + ".yml", false);
        }
        messageConfig = YamlConfiguration.loadConfiguration(languageFile);
    }

    public String getMessage(Messages messageKey, Object... args) {
        String path = messageKey.getPath();
        String message = messageConfig.getString(path, "Message not found: " + path);
        return ChatColor.translateAlternateColorCodes('&', String.format(message, args));
    }
}
