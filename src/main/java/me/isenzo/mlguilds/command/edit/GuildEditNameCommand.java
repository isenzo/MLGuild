package me.isenzo.mlguilds.command.edit;

import me.isenzo.mlguilds.Main;
import me.isenzo.mlguilds.guild.repository.GuildRepository;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class GuildEditNameCommand implements CommandExecutor {

    private final GuildRepository guildRepository;

    public GuildEditNameCommand(Main plugin) {
        this.guildRepository = plugin.getGuildRepository();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return false;
    }
}
