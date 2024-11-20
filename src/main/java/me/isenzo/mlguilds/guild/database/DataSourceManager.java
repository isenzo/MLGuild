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
        HikariConfig config = new HikariConfig();

        config.setJdbcUrl("jdbc:postgresql://localhost:5432/mlguilds"); // URL do bazy danych
        config.setUsername("username"); // Nazwa użytkownika bazy danych
        config.setPassword("password"); // Hasło do bazy danych

        // Optymalizacja ustawień HikariCP
        config.setMaximumPoolSize(50); // Zwiększona liczba maksymalnych połączeń w puli
        config.setMinimumIdle(10); // Minimalna liczba bezczynnych połączeń
        config.setIdleTimeout(30000); // Czas bezczynności połączenia przed jego zamknięciem
        config.setMaxLifetime(1800000); // Maksymalny czas życia połączenia
        config.setConnectionTimeout(10000); // Czas oczekiwania na połączenie

        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");

        this.dataSource = new HikariDataSource(config);
    }
}
