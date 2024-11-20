package me.isenzo.mlguilds.command;

import me.isenzo.mlguilds.Main;
import me.isenzo.mlguilds.command.create.GuildCreateCommand;
import me.isenzo.mlguilds.command.delete.GuildDeleteCommand;
import me.isenzo.mlguilds.command.deposit.GuildDepositCommand;
import me.isenzo.mlguilds.command.edit.GuildEditNameCommand;
import me.isenzo.mlguilds.guild.repository.GuildRepository;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import java.util.HashMap;
import java.util.Map;

public class GuildCommand implements CommandExecutor {

    private final Map<String, CommandExecutor> subCommands;
    private final GuildRepository guildRepository;

    public GuildCommand(Main plugin, GuildRepository guildRepository) {
        this.guildRepository = guildRepository;
        this.subCommands = new HashMap<>();

        subCommands.put("zaloz", new GuildCreateCommand(plugin));
        subCommands.put("usun", new GuildDeleteCommand());
        subCommands.put("depozyt", new GuildDepositCommand(plugin));
        subCommands.put("zmien", new GuildEditNameCommand(plugin));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if ((sender instanceof ConsoleCommandSender)) {
            sender.sendMessage("Tę komendę może wykonać tylko gracz!");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("Użycie: /gildia <stworz|usun|zapros> [argumenty]");
            return true;
        }

        String subCommand = args[0].toLowerCase();

        if (!subCommands.containsKey(subCommand)) {
            sender.sendMessage("Nieznana komenda. Użycie: /gildia <stworz|usun|zapros> [argumenty]");
            return true;
        }

        String[] subArgs = new String[args.length - 1];
        System.arraycopy(args, 1, subArgs, 0, args.length - 1);

        return subCommands.get(subCommand).onCommand(sender, command, label, subArgs);
    }
}
