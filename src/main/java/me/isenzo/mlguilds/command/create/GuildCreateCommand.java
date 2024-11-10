package me.isenzo.mlguilds.command.create;

import me.isenzo.mlguilds.Main;
import me.isenzo.mlguilds.guild.database.GuildData;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GuildCreateCommand implements CommandExecutor {

    private final Main plugin;
    private final GuildData guildData;

    public GuildCreateCommand(Main plugin) {
        this.plugin = plugin;
        this.guildData = plugin.getGuildData();
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!isPlayer(sender)) {
            return true;
        }

        Player player = (Player) sender;
        if (!canPlayerCreateGuild(player)) {
            return true;
        }

        if (!areArgumentsValid(sender, args)) {
            return true;
        }

        String guildName = args[0];
        String guildTag = args[1];

        plugin.getGuildItemsGUI().setGuildCreationInfo(player.getUniqueId(), guildName, guildTag);
        plugin.getGuildItemsGUI().openInventory(player);

        return true;
    }

    private boolean canPlayerCreateGuild(Player player) {
        if (!guildData.canPlayerCreateGuild(player.getUniqueId().toString())) {
            player.sendMessage(ChatColor.RED + "Nie możesz utworzyć gildii, ponieważ już jesteś w jednej");
            return false;
        }
        return true;
    }

    private boolean isPlayer(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Tę komendę może używać tylko gracz!");
            return false;
        }
        return true;
    }

    private boolean areArgumentsValid(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Użyj: /g stworz <nazwa> <tag>");
            return false;
        }
        return true;
    }
}
