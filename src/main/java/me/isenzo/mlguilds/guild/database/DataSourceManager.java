package me.isenzo.mlguilds.guild.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import org.bukkit.plugin.Plugin;

import javax.sql.DataSource;

@Getter
public class DataSourceManager {

    private final DataSource dataSource;
    private final Plugin plugin;

    public DataSourceManager(Plugin plugin) {
        this.plugin = plugin;
        this.dataSource = setupDataSource();
    }

    private DataSource setupDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:postgresql://localhost:5432/mlguilds");
        config.setUsername("postgres");
        config.setPassword("your_password");

        plugin.getLogger().info("Configuring data source...");
        return new HikariDataSource(config);
    }

    public void disconnect() {
        if (dataSource instanceof HikariDataSource) {
            plugin.getLogger().info("Closing data source connection...");
            ((HikariDataSource) dataSource).close();
        }
    }
}
