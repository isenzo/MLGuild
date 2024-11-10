package me.isenzo.mlguilds.file;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

@Getter
public class ConfigManager {
    private final JavaPlugin plugin;
    private FileConfiguration guildItemsConfig;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadGuildItemsConfig();
    }

    private void loadGuildItemsConfig() {
        try {
            File configFile = new File(plugin.getDataFolder(), "GuildItems.yml");
            if (!configFile.exists()) {
                plugin.saveResource("GuildItems.yml", false);
            }
            guildItemsConfig = YamlConfiguration.loadConfiguration(configFile);
            plugin.getLogger().info("GuildItems.yml loaded successfully.");
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Could not load GuildItems.yml", e);
        }
    }

    public int getGuildCreationCost() {
        return guildItemsConfig.getInt("cost", 0);
    }

    public Map<Material, Integer> getRequiredItemsForGuildCreation() {
        Map<Material, Integer> requiredItems = new HashMap<>();
        ConfigurationSection itemsSection = guildItemsConfig.getConfigurationSection("items");
        if (itemsSection != null) {
            for (String key : itemsSection.getKeys(false)) {
                Material material = Material.matchMaterial(key);
                if (material != null) {
                    int amount = itemsSection.getInt(key);
                    requiredItems.put(material, amount);
                } else {
                    plugin.getLogger().warning("§8[§2MlGuilds§8] §cUnknown material in §eGuildItems.yml: §a" + key);
                }
            }
        }
        return requiredItems;
    }
}
