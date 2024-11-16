package me.isenzo.mlguilds.player.events;

import lombok.Getter;
import me.isenzo.mlguilds.guild.repository.GuildRepository;
import me.isenzo.mlguilds.guild.repository.PlayerRepository;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

@Getter
public class PlayerJoinListener implements Listener {

    private final PlayerRepository playerRepository;

    public PlayerJoinListener(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        playerRepository.addPlayerIfNotExists(player);
    }
}
