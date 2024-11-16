package me.isenzo.mlguilds.guild.database;

import lombok.Getter;
import me.isenzo.mlguilds.Main;
import me.isenzo.mlguilds.data.PlayerData;
import me.isenzo.mlguilds.guild.GuildInfo;
import me.isenzo.mlguilds.guild.repository.GuildRepository;
import me.isenzo.mlguilds.guild.repository.PlayerRepository;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class DataCacheManager {

    private final Main plugin;
    private final GuildRepository guildRepository;
    private final PlayerRepository playerRepository;

    private final Map<UUID, PlayerData> playerDataCache = new ConcurrentHashMap<>();
    private final Map<Integer, GuildInfo> guildInforDataCache = new ConcurrentHashMap<>();

    private final Map<UUID, Long> playerLastSeenMap = new ConcurrentHashMap<>();
    private final Long CACHE_EXPIRATION = 20 * 60 * 1000L; //5 mins

    public CompletableFuture<PlayerData> getPlayerData(UUID uuid) {
        Long currentTime = System.currentTimeMillis();

        if(playerDataCache.containsKey(uuid)) {
            Long lastSeen = playerLastSeenMap.getOrDefault(uuid, 0L);
            if((currentTime - lastSeen) < CACHE_EXPIRATION) {
                return CompletableFuture.completedFuture(playerDataCache.get(uuid));
            }
        }

        return playerRepository.getPlayerData(uuid);

    }

    public DataCacheManager(Main plugin, GuildRepository guildRepository, PlayerRepository playerRepository) {
        this.plugin = plugin;
        this.guildRepository = guildRepository;
        this.playerRepository = playerRepository;
    }

}
