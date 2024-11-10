package me.isenzo.mlguilds.guild.database;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import javax.sql.DataSource;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

import static me.isenzo.mlguilds.guild.database.SQLQueries.*;

@Getter
public class GuildData {
    private final DataSource dataSource;
    private final Plugin plugin;

    public GuildData(DataSourceManager dataSourceManager) {
        this.plugin = dataSourceManager.getPlugin();
        this.dataSource = dataSourceManager.getDataSource();

        try {
            Class.forName("org.postgresql.Driver");
            plugin.getLogger().info("PostgreSQL driver loaded successfully.");
        } catch (ClassNotFoundException e) {
            plugin.getLogger().severe("Failed to load PostgreSQL driver: " + e.getMessage());
        }
    }

    public void initializeDatabase() {
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            statement.execute(CREATE_PLAYERS_TABLE.getQuery());
            statement.execute(CREATE_GUILDS_TABLE.getQuery());
            statement.execute(CREATE_GUILD_REQUIRED_ITEMS_TABLE.getQuery());
        } catch (SQLException e) {
            plugin.getLogger().severe(e.getMessage());
        }
    }

    public void addGuild(String name, String tag, Player founder) throws SQLException {
        try (Connection conn = getDataSource().getConnection()) {
            try (PreparedStatement insertGuildStmt = conn.prepareStatement(ADD_GUILD.getQuery())) {
                insertGuildStmt.setString(1, name);
                insertGuildStmt.setString(2, tag);
                insertGuildStmt.setString(3, founder.getUniqueId().toString());
                insertGuildStmt.executeUpdate();
                plugin.getLogger().info("Gildia została dodana do bazy danych!");
            }

            try (PreparedStatement updatePlayerStmt = conn.prepareStatement(UPDATE_PLAYER_AFTER_GUILD_CREATION.getQuery())) {
                updatePlayerStmt.setString(1, name);
                updatePlayerStmt.setString(2, founder.getUniqueId().toString());
                updatePlayerStmt.executeUpdate();
            }

        } catch (SQLException e) {
            plugin.getLogger().severe("Błąd podczas dodawania gildii do bazy danych: " + e.getMessage());
            throw e;
        }
    }

    public void addPlayerIfNotExists(Player player) {
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
    }

    public boolean isPlayerInGuild(String playerUUID) {
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
    }

    public boolean doesGuildExist(String guildName) {
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
    }

    public void updateDepositedItems(String playerUUID, String item, int amount) {
        try (PreparedStatement pstmt = dataSource.getConnection().prepareStatement(UPDATE_DEPOSITED_ITEMS.getQuery())) {
            pstmt.setString(1, playerUUID);
            pstmt.setString(2, item);
            pstmt.setInt(3, amount);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Błąd podczas aktualizacji wpłaconych przedmiotów: " + e.getMessage());
        }
    }

    public void updateDepositedMoney(String playerUUID, double amount) {
        try (PreparedStatement pstmt = dataSource.getConnection().prepareStatement(UPDATE_DEPOSITED_MONEY.getQuery())) {
            pstmt.setString(1, playerUUID);
            pstmt.setDouble(2, amount);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Błąd podczas aktualizacji wpłaconych pieniędzy: " + e.getMessage());
        }
    }

    public boolean canPlayerCreateGuild(String playerUUID) {
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
    }

    public Map<String, Integer> getDepositedItems(String playerUUID) {
        Map<String, Integer> depositedItems = new HashMap<>();
        try (Connection connection = dataSource.getConnection(); PreparedStatement pstmt =
                connection.prepareStatement(GET_DEPOSITED_ITEMS.getQuery())) {
            pstmt.setString(1, playerUUID);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    depositedItems.put(rs.getString("item"), rs.getInt("amount"));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe(e.getMessage());
        }
        return depositedItems;
    }

    public double getDepositedMoney(String playerUUID) {
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
    }

    public void addPlayerToGuild(String playerUUID, int guildId) {
        try (Connection conn = getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(ADD_PLAYER_TO_GUILD.getQuery())) {
            pstmt.setInt(1, guildId);
            pstmt.setString(2, playerUUID);
            pstmt.executeUpdate();
            plugin.getLogger().info("Gracz " + playerUUID + " dołączył do gildii " + guildId);
        } catch (SQLException e) {
            plugin.getLogger().severe("Błąd podczas dodawania gracza do gildii: " + e.getMessage());
        }
    }
}
