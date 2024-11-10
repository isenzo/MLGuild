package me.isenzo.mlguilds.db_config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

public class HikariCPConfig {
    private DataSource dataSource;

    public void setupDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:postgresql://localhost:5432/mlguilds");
        config.setUsername("postgres");
        config.setPassword("your_password");
        this.dataSource = new HikariDataSource(config);
    }
}
