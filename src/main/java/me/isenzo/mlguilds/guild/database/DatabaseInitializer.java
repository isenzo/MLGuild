package me.isenzo.mlguilds.guild.database;

import org.bukkit.plugin.Plugin;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.CompletableFuture;

public class DatabaseInitializer {

    private final DataSource dataSource;
    private final Plugin plugin;

    public void initializeDatabase() {
        CompletableFuture.runAsync(() -> {
            try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
                statement.execute(SQLQueries.CREATE_PLAYERS_TABLE.getQuery());
                statement.execute(SQLQueries.CREATE_GUILDS_TABLE.getQuery());
                statement.execute(SQLQueries.CREATE_GUILD_REQUIRED_ITEMS_TABLE.getQuery());
                plugin.getLogger().info("Initialized database successfully");
            } catch (SQLException e) {
                plugin.getLogger().severe("Error initializing database: " + e.getMessage());
            }
        });
    }

    public DatabaseInitializer(DataSourceManager dataSourceManager, Plugin plugin) {
        this.dataSource = dataSourceManager.getDataSource();
        this.plugin = plugin;

        try {
            Class.forName("org.postgresql.Driver");
            plugin.getLogger().info("PostgreSQL driver loaded successfully.");
        } catch (ClassNotFoundException e) {
            plugin.getLogger().severe("Failed to load PostgreSQL driver: " + e.getMessage());
        }
    }
}
