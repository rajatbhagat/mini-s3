package com.rajat.minis3.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

/**
 * Database Configuration
 *
 * This class demonstrates how to create a custom singleton DataSource bean.
 * Note: Spring Boot auto-configuration already creates a DataSource from application.yml,
 * so this is optional and only needed if you want custom configuration beyond what
 * application.yml provides.
 *
 * Key Concepts:
 * - @Configuration: Marks this as a Spring configuration class
 * - @Bean: Methods annotated with @Bean create singleton beans (by default)
 * - Singleton Scope: Only ONE instance of the DataSource exists in the application
 * - HikariCP: Fast, lightweight connection pool
 */
@Configuration
@Slf4j
public class DatabaseConfig {

    @Value("${spring.datasource.url}")
    private String jdbcUrl;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${spring.datasource.hikari.maximum-pool-size:10}")
    private int maximumPoolSize;

    @Value("${spring.datasource.hikari.minimum-idle:5}")
    private int minimumIdle;

    @Value("${spring.datasource.hikari.connection-timeout:20000}")
    private long connectionTimeout;

    /**
     * Creates a singleton HikariCP DataSource bean
     *
     * Singleton means:
     * - Created ONCE when application starts
     * - Same instance shared across entire application
     * - Thread-safe connection pooling
     *
     * HikariCP manages a pool of database connections:
     * - Keeps connections open and ready to use
     * - Much faster than creating new connections for each query
     * - Automatically handles connection lifecycle
     *
     * @return DataSource singleton bean
     */
    @Bean
    @Profile("!test")  // Don't use this bean in tests (use H2 instead)
    public DataSource dataSource() {
        log.info("Creating HikariCP DataSource singleton bean");
        log.info("JDBC URL: {}", jdbcUrl);
        log.info("Pool size: {} (max), {} (min idle)", maximumPoolSize, minimumIdle);

        HikariConfig config = new HikariConfig();

        // Basic connection settings
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName("org.postgresql.Driver");

        // Connection pool settings
        config.setMaximumPoolSize(maximumPoolSize);
        config.setMinimumIdle(minimumIdle);
        config.setConnectionTimeout(connectionTimeout);
        config.setIdleTimeout(300000);  // 5 minutes
        config.setMaxLifetime(1200000); // 20 minutes

        // Performance optimizations
        config.setAutoCommit(true);
        config.setConnectionTestQuery("SELECT 1");

        // Pool name for monitoring/debugging
        config.setPoolName("MiniS3-HikariCP");

        // Leak detection (helps find connection leaks during development)
        config.setLeakDetectionThreshold(60000); // 60 seconds

        HikariDataSource dataSource = new HikariDataSource(config);

        log.info("HikariCP DataSource created successfully");
        log.info("Pool name: {}", dataSource.getPoolName());

        return dataSource;
    }

    /**
     * Example: Database health check bean
     * This is another singleton that depends on the DataSource
     */
    @Bean
    public DatabaseHealthCheck databaseHealthCheck(DataSource dataSource) {
        log.info("Creating DatabaseHealthCheck singleton bean");
        return new DatabaseHealthCheck(dataSource);
    }
}
