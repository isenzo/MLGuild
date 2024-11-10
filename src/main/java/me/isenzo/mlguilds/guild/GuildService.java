package me.isenzo.mlguilds.guild;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.isenzo.mlguilds.Main;
import me.isenzo.mlguilds.command.validation.PlayerCommandValidation;
import me.isenzo.mlguilds.guild.database.GuildData;
import me.isenzo.mlguilds.message.MessageUtils;
import me.isenzo.mlguilds.message.Messages;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.Objects;

@RequiredArgsConstructor
@Getter
public class GuildService {

    private final Main plugin;
    private final GuildData guildData;
    private final PlayerCommandValidation validation;

    public boolean createGuild(Player player, String guildName, String guildTag) {
        guildData.addPlayerIfNotExists(player);
        String validationMessage = validation.validateGuildCreation(player, guildName, guildTag);

        if (Objects.nonNull(validationMessage)) {
            player.sendMessage(ChatColor.RED + validationMessage);
            return false;
        }

        if (!plugin.getGuildItemsGUI().allItemsDeposited(player)) {
            MessageUtils.sendToPlayer(player, Messages.REQUIREMENTS_NOT_MET, guildName, guildTag);
            return false;
        }

        try {
            guildData.addGuild(guildName, guildTag, player);
            MessageUtils.sendToPlayer(player, Messages.GUILD_CREATE_SUCCESS, guildName, guildTag);
            return true;
        } catch (SQLException e) {
            MessageUtils.sendToPlayer(player, Messages.GUILD_CREATION_ERROR, guildName, guildTag);
            return false;
        }
    }
}
