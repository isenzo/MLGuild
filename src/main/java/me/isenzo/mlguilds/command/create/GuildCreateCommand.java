package me.isenzo.mlguilds.command.create;

import lombok.RequiredArgsConstructor;
import me.isenzo.mlguilds.Main;
import me.isenzo.mlguilds.guild.repository.GuildRepository;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class GuildCreateCommand implements CommandExecutor {

    private final GuildRepository guildRepository;

    public GuildCreateCommand(Main plugin) {
        this.guildRepository = plugin.getGuildRepository();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!isPlayer(sender)) {
            return true;
        }

        Player player = (Player) sender;

        String playerUUID = player.getUniqueId().toString();

        // Sprawdzanie, czy gracz już jest w trakcie tworzenia gildii
        guildRepository.isPlayerInCreateProcess(playerUUID).thenAccept(isInCreateProcess -> {
            if (isInCreateProcess) {
                Bukkit.getScheduler().runTask(guildRepository.getPlugin(), () ->
                        player.sendMessage(ChatColor.RED + "Jesteś już w trakcie tworzenia gildii. Użyj /g depozyt, aby wpłacić potrzebne przedmioty."));
                return;
            }

            // Sprawdzenie, czy gracz może utworzyć gildię
            guildRepository.canPlayerCreateGuild(playerUUID).thenAccept(canCreateGuild -> {
                if (!canCreateGuild) {
                    Bukkit.getScheduler().runTask(guildRepository.getPlugin(), () ->
                            player.sendMessage(ChatColor.RED + "Nie możesz utworzyć gildii, ponieważ już jesteś w jednej. Opuść obecną gildię, aby utworzyć nową."));
                    return;
                }

                // Sprawdzenie, czy argumenty są prawidłowe
                if (!areArgumentsValid(sender, args)) {
                    return;
                }

                String guildName = args[0];
                String guildTag = args[1];

                // Utworzenie gildii w stanie "w trakcie tworzenia"
                guildRepository.addGuildInProgress(guildName, guildTag, player).thenAccept(success -> {
                    if (success) {
                        guildRepository.updatePlayerInProgressStatus(playerUUID, true).thenRun(() -> {
                            Bukkit.getScheduler().runTask(guildRepository.getPlugin(), () -> {
                                // Wysłanie komunikatów na czacie
                                player.sendMessage(ChatColor.GREEN + "MLGuilds >> Założyłeś gildię " + guildName + " " + guildTag + ".");
                                player.sendMessage(ChatColor.YELLOW + "MLGuilds >> By stworzyć gildię, musisz wpłacić wymaganą ilość przedmiotów i pieniędzy, wpisz /g depozyt.");
                                player.sendMessage(ChatColor.YELLOW + "MLGuilds >> Obecnie możesz zmienić nazwę i tag za darmo, wpisując /g zmien nazwa|tag NowaNazwa lub NowyTag.");
                                player.sendMessage(ChatColor.YELLOW + "MLGuilds >> Możesz zaprosić do 4 osób, z którymi będziesz szybciej mógł zebrać wymaganą ilość pieniędzy.");
                                player.sendMessage(ChatColor.YELLOW + "MLGuilds >> Jeżeli potrzebujesz pomocy, pamiętaj, że administracja pozostaje do Twojej dyspozycji :)");
                                player.sendMessage(ChatColor.YELLOW + "MLGuilds >> Miłej gry!");
                            });
                        });
                    } else {
                        Bukkit.getScheduler().runTask(guildRepository.getPlugin(), () ->
                                player.sendMessage(ChatColor.RED + "Nie udało się rozpocząć procesu tworzenia gildii. Spróbuj ponownie."));
                    }
                });
            });
        });

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
            sender.sendMessage(ChatColor.RED + "Użyj: /g zaloz <nazwa> <tag>");
            return false;
        }
        return true;
    }
}
