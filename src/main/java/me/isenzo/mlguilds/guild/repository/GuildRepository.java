package me.isenzo.mlguilds.guild.repository;

import lombok.Getter;
import me.isenzo.mlguilds.guild.GuildInfo;
import me.isenzo.mlguilds.guild.database.DataSourceManager;
import me.isenzo.mlguilds.guild.database.SQLQueries;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import javax.sql.DataSource;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static me.isenzo.mlguilds.guild.database.SQLQueries.*;

@Getter
public class GuildRepository {
    private final DataSource dataSource;
    private final Plugin plugin;

    private final Map<String, GuildInfo> guildCache = new ConcurrentHashMap<>();

    public CompletableFuture<Boolean> addGuildAsync(String name, String tag, Player founder) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = dataSource.getConnection()) {
                try (PreparedStatement insertGuildStmt = conn.prepareStatement(SQLQueries.ADD_GUILD.getQuery())) {
                    insertGuildStmt.setString(1, name);
                    insertGuildStmt.setString(2, tag);
                    insertGuildStmt.setString(3, founder.getUniqueId().toString());
                    insertGuildStmt.executeUpdate();
                    plugin.getLogger().info("Gildia została dodana do bazy danych!");
                }

                try (PreparedStatement updatePlayerStmt = conn.prepareStatement(SQLQueries.UPDATE_PLAYER_AFTER_GUILD_CREATION.getQuery())) {
                    updatePlayerStmt.setString(1, name);
                    updatePlayerStmt.setString(2, founder.getUniqueId().toString());
                    updatePlayerStmt.executeUpdate();
                }

                return true;
            } catch (SQLException e) {
                plugin.getLogger().severe("Błąd podczas dodawania gildii do bazy danych: " + e.getMessage());
                return false;
            }
        });
    }

    public CompletableFuture<Boolean> addGuildInProgress(String guildName, String guildTag, Player founder) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = SQLQueries.ADD_GUILD_IN_PROGRESS.getQuery();
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement pstmt = connection.prepareStatement(sql)) {

                pstmt.setString(1, guildName);
                pstmt.setString(2, guildTag);
                pstmt.setString(3, founder.getUniqueId().toString());
                pstmt.setInt(4, 1);

                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    plugin.getLogger().info("Dodano gildię w trakcie tworzenia: " + guildName + " założoną przez gracza: " + founder.getName());
                    return true;
                } else {
                    plugin.getLogger().warning("Nie udało się dodać gildii: " + guildName + " założonej przez gracza: " + founder.getName());
                    return false;
                }

            } catch (SQLException e) {
                plugin.getLogger().severe("Błąd podczas dodawania gildii w trakcie tworzenia: " + e.getMessage());
                return false;
            }
        });
    }

    public CompletableFuture<Boolean> playerExists(String playerUUID) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement pstmt = connection.prepareStatement(CHECK_PLAYER_EXISTS.getQuery())) {
                pstmt.setString(1, playerUUID);
                try (ResultSet rs = pstmt.executeQuery()) {
                    return rs.next() && rs.getInt(1) > 0;
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Błąd podczas sprawdzania istnienia gracza w bazie danych: " + e.getMessage());
            }
            return false;
        });
    }

    public CompletableFuture<Boolean> isPlayerInGuild(String playerUUID) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement pstmt = connection.prepareStatement(CHECK_PLAYER_IN_GUILD.getQuery())) {
                pstmt.setString(1, playerUUID);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getBoolean("is_player_in_guild");
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Błąd podczas sprawdzania, czy gracz jest w gildii: " + e.getMessage());
            }
            return false;
        });
    }

    public CompletableFuture<Boolean> isPlayerInCreatedGuild(String playerUUID) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement pstmt = connection.prepareStatement(SQLQueries.IS_PLAYER_IN_CREATED_GUILD.getQuery())) {
                pstmt.setString(1, playerUUID);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getBoolean("is_created");
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Błąd podczas sprawdzania, czy gracz jest w utworzonej gildii: " + e.getMessage());
            }
            return false;
        });
    }

    public CompletableFuture<Boolean> isPlayerInCreateProcess(String playerUUID) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement pstmt = connection.prepareStatement(IS_PLAYER_IN_CREATE_PROCESS.getQuery())) {
                pstmt.setString(1, playerUUID);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getBoolean("is_creating_guild");
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Błąd podczas sprawdzania, czy gracz jest w procesie tworzenia gildii: " + e.getMessage());
            }
            return false;
        });
    }

    public CompletableFuture<Boolean> isFounder(String playerUUID) {
        return CompletableFuture.supplyAsync(() -> {
            boolean isFounder = false;
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement pstmt = connection.prepareStatement(SQLQueries.IS_PLAYER_FOUNDER.getQuery())) {
                pstmt.setString(1, playerUUID);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        int founderId = rs.getInt("founder_id");
                        try (PreparedStatement playerStmt = connection.prepareStatement(GET_PLAYER_ID.getQuery())) {
                            playerStmt.setString(1, playerUUID);
                            try (ResultSet playerRs = playerStmt.executeQuery()) {
                                if (playerRs.next() && playerRs.getInt("id") == founderId) {
                                    isFounder = true;
                                }
                            }
                        }
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Błąd podczas sprawdzania, czy gracz jest założycielem gildii: " + e.getMessage());
            }
            return isFounder;
        });
    }

    public CompletableFuture<Boolean> doesGuildExist(String guildName) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement pstmt = connection.prepareStatement(DOES_GUILD_EXIST.getQuery())) {
                pstmt.setString(1, guildName);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1) > 0;
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().severe(e.getMessage());
            }
            return false;
        });
    }

    public void updateDepositedItems(String playerUUID, String item, int amount) {
        CompletableFuture.runAsync(() -> {
            try (PreparedStatement pstmt = dataSource.getConnection().prepareStatement(UPDATE_DEPOSITED_ITEMS.getQuery())) {
                pstmt.setString(1, playerUUID);
                pstmt.setString(2, item);
                pstmt.setInt(3, amount);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("Błąd podczas aktualizacji wpłaconych przedmiotów: " + e.getMessage());
            }
        });
    }

    public void updateDepositedMoney(String playerUUID, double amount) {
        CompletableFuture.runAsync(() -> {
            try (PreparedStatement pstmt = dataSource.getConnection().prepareStatement(UPDATE_DEPOSITED_MONEY.getQuery())) {
                pstmt.setString(1, playerUUID);
                pstmt.setDouble(2, amount);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("Błąd podczas aktualizacji wpłaconych pieniędzy: " + e.getMessage());
            }
        });
    }

    public CompletableFuture<Boolean> updatePlayerGuildStatus(String playerUUID, boolean isInGuild) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement pstmt = connection.prepareStatement(SQLQueries.UPDATE_PLAYER_GUILD_STATUS.getQuery())) {
                pstmt.setBoolean(1, isInGuild);
                pstmt.setString(2, playerUUID);
                int rowsAffected = pstmt.executeUpdate();
                return rowsAffected > 0;
            } catch (SQLException e) {
                plugin.getLogger().severe("Błąd podczas aktualizacji statusu gracza w gildii: " + e.getMessage());
                return false;
            }
        });
    }

    public CompletableFuture<Boolean> updateGuildMemberCount(String guildName, int increment) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement pstmt = connection.prepareStatement(SQLQueries.UPDATE_GUILD_MEMBER_COUNT.getQuery())) {
                pstmt.setInt(1, increment);
                pstmt.setString(2, guildName);
                int rowsAffected = pstmt.executeUpdate();
                return rowsAffected > 0;
            } catch (SQLException e) {
                plugin.getLogger().severe("Błąd podczas aktualizacji liczby członków gildii: " + e.getMessage());
                return false;
            }
        });
    }

    public CompletableFuture<Void> updatePlayerCreatingGuildStatus(String playerUUID, boolean isCreatingGuild) {
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement pstmt = connection.prepareStatement(UPDATE_PLAYER_CREATING_GUILD_STATUS.getQuery())) {

                pstmt.setBoolean(1, isCreatingGuild);
                pstmt.setString(2, playerUUID);

                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    plugin.getLogger().info("Zaktualizowano status tworzenia gildii dla gracza: " + playerUUID + " na: " + isCreatingGuild);
                } else {
                    plugin.getLogger().warning("Nie znaleziono gracza o UUID: " + playerUUID + " przy próbie aktualizacji statusu tworzenia gildii.");
                }

            } catch (SQLException e) {
                plugin.getLogger().severe("Błąd podczas aktualizacji statusu tworzenia gildii dla gracza: " + e.getMessage());
            }
        });
    }


    public CompletableFuture<Void> updatePlayerInProgressStatus(String playerUUID, boolean isCreatingGuild) {
        return CompletableFuture.runAsync(() -> {
            String sql = SQLQueries.UPDATE_PLAYER_IN_PROGRESS_STATUS.getQuery();

            try (Connection connection = dataSource.getConnection();
                 PreparedStatement pstmt = connection.prepareStatement(sql)) {

                pstmt.setBoolean(1, isCreatingGuild);
                pstmt.setString(2, playerUUID);

                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    plugin.getLogger().info("Zaktualizowano status tworzenia gildii dla gracza: " + playerUUID + " na: " + isCreatingGuild);
                } else {
                    plugin.getLogger().warning("Nie znaleziono gracza o UUID: " + playerUUID + " przy próbie aktualizacji statusu tworzenia gildii.");
                }

            } catch (SQLException e) {
                plugin.getLogger().severe("Błąd podczas aktualizacji statusu tworzenia gildii dla gracza: " + e.getMessage());
            }
        });
    }

    public CompletableFuture<Void> updatePlayerGuildId(String playerUUID, int guildId) {
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement pstmt = connection.prepareStatement("UPDATE players SET guild_id = ? WHERE uuid = ?")) {
                pstmt.setInt(1, guildId);
                pstmt.setString(2, playerUUID);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("Błąd podczas aktualizacji guild_id dla gracza: " + e.getMessage());
            }
        });
    }

    public CompletableFuture<Boolean> canPlayerCreateGuild(String playerUUID) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement pstmt = connection.prepareStatement(CAN_PLAYER_CREATE_GUILD.getQuery())) {
                pstmt.setString(1, playerUUID);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getBoolean("can_create_guild");
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().severe(e.getMessage());
            }
            return false;
        });
    }

    public CompletableFuture<Optional<GuildInfo>> getGuildInfoByName(String guildName) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = GET_GUILD_INFO_BY_NAME.getQuery();
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, guildName);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(new GuildInfo(
                                rs.getInt("id"),
                                rs.getString("name"),
                                rs.getString("tag"),
                                rs.getString("founder_id"),
                                rs.getInt("member_count"),
                                rs.getBoolean("is_created")
                        ));
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Błąd podczas pobierania informacji o gildii: " + e.getMessage());
            }
            return Optional.empty();
        });
    }

    public CompletableFuture<Optional<Integer>> getGuildIDByName(String guildName) {
        return getGuildInfoByName(guildName).thenApply(optionalGuildInfo ->
                optionalGuildInfo.map(GuildInfo::getId)
        );
    }

    public CompletableFuture<String> getGuildNameByPlayerUUID(String playerUUID) {
        return CompletableFuture.supplyAsync(() -> {
            String guildName = null;

            try (Connection connection = dataSource.getConnection();
                 PreparedStatement pstmt = connection.prepareStatement(SQLQueries.GET_GUILD_NAME_BY_PLAYER_UUID.getQuery())) {
                pstmt.setString(1, playerUUID);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        guildName = rs.getString("name");
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Błąd podczas pobierania nazwy gildii: " + e.getMessage());
            }
            return guildName;
        });
    }

    public CompletableFuture<String> getGuildTagByPlayerUUID(String playerUUID) {
        return CompletableFuture.supplyAsync(() -> {
            String guildTag = null;

            try (Connection connection = dataSource.getConnection();
                 PreparedStatement pstmt = connection.prepareStatement(SQLQueries.GET_GUILD_TAG_BY_PLAYER_UUID.getQuery())) {
                pstmt.setString(1, playerUUID);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        guildTag = rs.getString("tag");
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Błąd podczas pobierania tagu gildii: " + e.getMessage());
            }
            return guildTag;
        });
    }

    public CompletableFuture<Map<String, Integer>> getDepositedItems(String playerUUID) {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, Integer> depositedItems = new HashMap<>();
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement pstmt = connection.prepareStatement(GET_DEPOSITED_ITEMS.getQuery())) {
                pstmt.setString(1, playerUUID);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        depositedItems.put(rs.getString("item"), rs.getInt("amount"));
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Błąd podczas pobierania wpłaconych przedmiotów: " + e.getMessage());
            }
            return depositedItems;
        });
    }

    public CompletableFuture<Double> getDepositedMoney(String playerUUID) {
        return CompletableFuture.supplyAsync(() -> {
            double depositedMoney = 0;
            try (PreparedStatement pstmt = dataSource.getConnection().prepareStatement(GET_DEPOSITED_MONEY.getQuery())) {
                pstmt.setString(1, playerUUID);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        depositedMoney = rs.getDouble("amount");
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Błąd podczas pobierania wpłaconych pieniędzy: " + e.getMessage());
            }
            return depositedMoney;
        });
    }

    public CompletableFuture<Void> addPlayerToGuild(String playerUUID, int guildId) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = getDataSource().getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(ADD_PLAYER_TO_GUILD.getQuery())) {
                pstmt.setInt(1, guildId);
                pstmt.setString(2, playerUUID);
                pstmt.executeUpdate();
                plugin.getLogger().info("Gracz " + playerUUID + " dołączył do gildii " + guildId);
            } catch (SQLException e) {
                plugin.getLogger().severe("Błąd podczas dodawania gracza do gildii: " + e.getMessage());
            }
        });
    }

    public GuildRepository(DataSourceManager dataSourceManager) {
        this.plugin = dataSourceManager.getPlugin();
        this.dataSource = dataSourceManager.getDataSource();
    }

}
