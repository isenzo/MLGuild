package me.isenzo.mlguilds.command.deposit;

import me.isenzo.mlguilds.Main;
import me.isenzo.mlguilds.guild.repository.GuildRepository;
import me.isenzo.mlguilds.message.MessageUtils;
import me.isenzo.mlguilds.message.Messages;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GuildDepositCommand implements CommandExecutor {

    private final Main plugin;
    private final GuildRepository guildRepository;

    public GuildDepositCommand(Main plugin) {
        this.plugin = plugin;
        this.guildRepository = plugin.getGuildRepository();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Tę komendę może używać tylko gracz!");
            return true;
        }

        Player player = (Player) sender;
        String playerUUID = player.getUniqueId().toString();

        guildRepository.isPlayerInGuild(playerUUID).thenAccept(isInGuild -> {
            if (!isInGuild) {
                Bukkit.getScheduler().runTask(plugin, () ->
                        MessageUtils.sendToPlayer(player, Messages.NOT_IN_ANY_GUILD)
                );
                return;
            }

            guildRepository.isPlayerInCreatedGuild(playerUUID).thenAccept(isPlayerInCreatedGuild -> {
                if (!isPlayerInCreatedGuild) {
                    Bukkit.getScheduler().runTask(plugin, () ->
                            MessageUtils.sendToPlayer(player, Messages.PLAYER_IN_ALREADY_CREATED_GUILD)
                    );
                    return;
                }
                Bukkit.getScheduler().runTask(plugin, () ->
                        plugin.getGuildItemsGUI().openInventory(player)
                );
            });
        });

        plugin.getGuildItemsGUI().openInventory(player);
        return true;
    }
}
