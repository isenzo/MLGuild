package me.isenzo.mlguilds;

import lombok.Getter;
import lombok.Setter;
import me.isenzo.mlguilds.command.GuildCommand;
import me.isenzo.mlguilds.command.validation.PlayerCommandValidation;
import me.isenzo.mlguilds.file.ConfigManager;
import me.isenzo.mlguilds.gui.GuildItemsGUI;
import me.isenzo.mlguilds.guild.GuildService;
import me.isenzo.mlguilds.guild.database.DataSourceManager;
import me.isenzo.mlguilds.guild.database.GuildData;
import me.isenzo.mlguilds.message.MessageManager;
import net.luckperms.api.LuckPerms;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Locale;
import java.util.Objects;

@Getter
@Setter
public final class Main extends JavaPlugin {

    private static Main instance;
    private GuildService guildService;
    private MessageManager messageManager;
    private GuildCommand guildCommand;
    private GuildData guildData;
    private Economy economyApi;
    private LuckPerms luckPermsApi;
//    private WorldEdit worldEditApi;
//    private WorldGuard worldGuardApi;
    private PlayerCommandValidation playerCommandValidation;
    private ConfigManager configManager;
    private GuildItemsGUI guildItemsGUI;
    private DataSourceManager dataSourceManager;

    @Override
    public void onEnable() {
        instance = this;

        if (!setupEconomy()) {
            getLogger().severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        economyApi = registerServiceProvider(Economy.class);
        luckPermsApi = registerServiceProvider(LuckPerms.class);

        this.dataSourceManager = new DataSourceManager(this);
        this.guildData = new GuildData(dataSourceManager);
        this.guildData.initializeDatabase();
        this.configManager = new ConfigManager(this);
        this.playerCommandValidation = new PlayerCommandValidation(this, guildData);
        this.messageManager = new MessageManager(this, new Locale("pl"));
        this.guildService = new GuildService(this, guildData, playerCommandValidation);
        this.guildCommand = new GuildCommand(this);
        this.guildItemsGUI = new GuildItemsGUI(this);

        Objects.requireNonNull(this.getCommand("guild")).setExecutor(guildCommand);
        getServer().getPluginManager().registerEvents(guildItemsGUI, this);
    }


    @Override
    public void onDisable() {
        getLogger().severe(String.format("[%s] - Shutting off database", getDescription().getName()));
        if (Objects.nonNull(dataSourceManager)) {
            dataSourceManager.disconnect();
        }
    }

    private <T> T registerServiceProvider(Class<T> serviceClass) {
        RegisteredServiceProvider<T> rsp = getServer().getServicesManager().getRegistration(serviceClass);
        if (Objects.isNull(rsp)) {
            getLogger().warning("Can not find " + serviceClass.getName() + ", switching off the plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return null;
        }
        return rsp.getProvider();
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            getLogger().severe("Vault not found");
            return false;
        }

        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            getLogger().severe("No economy provider found");
            return false;
        }

        economyApi = rsp.getProvider();
        return true;
    }

    public static MessageManager getMessageManager() {
        return instance.messageManager;
    }

    public static GuildService getGuildService() {
        return instance.guildService;
    }
}
