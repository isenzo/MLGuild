package me.isenzo.mlguilds.guild.repository;

import lombok.Getter;
import me.isenzo.mlguilds.data.PlayerData;
import me.isenzo.mlguilds.guild.database.DataSourceManager;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Jedis;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static me.isenzo.mlguilds.guild.database.SQLQueries.*;

@Getter
public class PlayerRepository {

    private final DataSource dataSource;
    private final Plugin plugin;
    private final JedisPool jedisPool;

    public PlayerRepository(DataSourceManager dataSourceManager, JedisPool jedisPool) {
        this.dataSource = dataSourceManager.getDataSource();
        this.plugin = dataSourceManager.getPlugin();
        this.jedisPool = jedisPool;
    }

    public void addPlayerIfNotExists(Player player) {
        CompletableFuture.runAsync(() -> {
            try (Connection conn = getDataSource().getConnection();
                 PreparedStatement checkStmt = conn.prepareStatement(CHECK_PLAYER_EXISTS.getQuery())) {
                checkStmt.setString(1, player.getUniqueId().toString());
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        try (PreparedStatement insertStmt = conn.prepareStatement(ADD_PLAYER.getQuery())) {
                            insertStmt.setString(1, player.getUniqueId().toString());
                            insertStmt.setString(2, player.getName());
                            insertStmt.executeUpdate();
                            plugin.getLogger().info("Dodano nowego gracza do tabeli players: " + player.getUniqueId());
                        }
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Błąd podczas dodawania gracza do tabeli players: " + e.getMessage());
            }
        });
    }

    public CompletableFuture<PlayerData> getPlayerData(UUID playerUUID) {
        return CompletableFuture.supplyAsync(() -> {
            PlayerData playerData = null;

            try (Jedis jedis = jedisPool.getResource()) {
                String key = "player:data:" + playerUUID.toString();
                if (jedis.exists(key)) {
                    String nickname = jedis.hget(key, "nickname");
                    int guildId = Integer.parseInt(jedis.hget(key, "guild_id"));
                    boolean isInGuild = Boolean.parseBoolean(jedis.hget(key, "is_player_in_guild"));
                    boolean canCreateGuild = Boolean.parseBoolean(jedis.hget(key, "can_create_guild"));
                    boolean isCreatingGuild = Boolean.parseBoolean(jedis.hget(key, "is_creating_guild"));
                    return new PlayerData(playerUUID, nickname, guildId, isInGuild, canCreateGuild, isCreatingGuild);
                }
            } catch (Exception e) {
                plugin.getLogger().severe("Błąd podczas pobierania danych gracza z Redis: " + e.getMessage());
            }

            try (Connection connection = dataSource.getConnection();
                 PreparedStatement pstmt = connection.prepareStatement(GET_PLAYER_DATA.getQuery())) {
                pstmt.setString(1, playerUUID.toString());
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        playerData = new PlayerData(playerUUID, rs.getString("nickname"), rs.getInt("guild_id"),
                                rs.getBoolean("is_player_in_guild"), rs.getBoolean("can_create_guild"), rs.getBoolean("is_creating_guild"));

                        // Zapisujemy dane do Redis
                        try (Jedis jedis = jedisPool.getResource()) {
                            String key = "player:data:" + playerUUID;
                            jedis.hset(key, "nickname", playerData.getNickname());
                            jedis.hset(key, "guild_id", String.valueOf(playerData.getGuildId()));
                            jedis.hset(key, "is_player_in_guild", String.valueOf(playerData.isInGuild()));
                            jedis.hset(key, "can_create_guild", String.valueOf(playerData.isCanCreateGuild()));
                            jedis.hset(key, "is_creating_guild", String.valueOf(playerData.isCreatingGuild()));
                            jedis.expire(key, 3600); // Ustawienie TTL na 1 godzinę
                        }
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Błąd podczas pobierania danych gracza z bazy danych: " + e.getMessage());
            }
            return playerData;
        });
    }
}
