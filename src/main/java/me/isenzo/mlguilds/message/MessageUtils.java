package me.isenzo.mlguilds.message;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MessageUtils {
    public static void sendToPlayer(Player player, Messages messageKey, Object... args) {
        String message = Messages.format(messageKey, args);
        player.sendMessage(message);
    }

    public static void sendToSender(CommandSender sender, Messages messageKey, Object... args) {
        String message = Messages.format(messageKey, args);
        sender.sendMessage(message);
    }
}
