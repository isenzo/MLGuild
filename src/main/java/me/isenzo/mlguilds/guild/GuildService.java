package me.isenzo.mlguilds.guild;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.isenzo.mlguilds.Main;
import me.isenzo.mlguilds.command.validation.PlayerCommandValidation;
import me.isenzo.mlguilds.guild.repository.GuildRepository;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@Getter
public class GuildService {

    private final Main plugin;
    private final GuildRepository guildRepository;
    private final PlayerCommandValidation validation;

    public boolean createGuild(Player player, String guildName, String guildTag) {
        String playerUUID = player.getUniqueId().toString();

        guildRepository.playerExists(playerUUID)
                .thenCompose(exists -> {
                    if (!exists) {
                        sendPlayerMessageAsync(player, ChatColor.RED + "Twoje dane nie zostały jeszcze zapisane w systemie. Spróbuj ponownie później.");
                        return CompletableFuture.completedFuture(false);
                    }
                    return guildRepository.isPlayerInGuild(playerUUID);
                })
                .thenCompose(isInGuild -> {
                    if (isInGuild) {
                        sendPlayerMessageAsync(player, ChatColor.RED + "Nie możesz utworzyć nowej gildii, ponieważ już należysz do jednej.");
                        return CompletableFuture.completedFuture(false);
                    }
                    return guildRepository.isPlayerInCreatedGuild(playerUUID);
                })
                .thenCompose(isInCreateProcess -> {
                    if (isInCreateProcess) {
                        sendPlayerMessageAsync(player, ChatColor.RED + "Już jesteś w trakcie tworzenia gildii. Wpisz /g depozyt, aby kontynuować proces.");
                        return CompletableFuture.completedFuture(false);
                    }

                    String validationMessage = validation.validateGuildCreation(player, guildName, guildTag);
                    if (validationMessage != null) {
                        sendPlayerMessageAsync(player, ChatColor.RED + validationMessage);
                        return CompletableFuture.completedFuture(false);
                    }

                    return guildRepository.updatePlayerCreatingGuildStatus(playerUUID, true)
                            .thenCompose(aVoid -> guildRepository.addGuildAsync(guildName, guildTag, player))
                            .thenCompose(success -> {
                                if (!success) {
                                    sendPlayerMessageAsync(player, "Wystąpił błąd podczas tworzenia gildii.");
                                    return CompletableFuture.completedFuture(false);
                                }
                                return guildRepository.getGuildIDByName(guildName)
                                        .thenCompose(guildIdOpt -> {
                                            if (guildIdOpt.isEmpty()) {
                                                return CompletableFuture.completedFuture(false);
                                            }
                                            int guildId = guildIdOpt.get();
                                            return guildRepository.updatePlayerGuildId(playerUUID, guildId)
                                                    .thenCompose(aVoid -> guildRepository.updatePlayerGuildStatus(playerUUID, true))
                                                    .thenCompose(v -> guildRepository.updateGuildMemberCount(guildName, 1))
                                                    .thenApply(v -> true);
                                        });
                            });
                })
                .thenAccept(finalSuccess -> {
                    if (finalSuccess) {
                        sendPlayerMessageAsync(player, ChatColor.GREEN + "Gildia została utworzona pomyślnie!");
                    } else {
                        sendPlayerMessageAsync(player, ChatColor.RED + "Wystąpił błąd podczas tworzenia gildii.");
                    }
                });

        return true;
    }

    private void sendPlayerMessageAsync(Player player, String message) {
        Bukkit.getScheduler().runTask(plugin, () -> player.sendMessage(message));
    }
}
