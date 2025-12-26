package com.rajat.minis3.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Database Health Check Utility
 *
 * This is a singleton bean that uses the DataSource singleton.
 * Demonstrates:
 * - How to inject and use the DataSource bean
 * - How singletons can depend on other singletons
 * - Connection pool usage
 */
@Slf4j
@RequiredArgsConstructor
public class DatabaseHealthCheck {

    private final DataSource dataSource;

    /**
     * Check if database is reachable and responsive
     *
     * How this works:
     * 1. Gets a connection from the pool (doesn't create a new one!)
     * 2. Executes a simple query
     * 3. Returns the connection to the pool
     *
     * @return true if database is healthy, false otherwise
     */
    public boolean isHealthy() {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT 1")) {

            if (resultSet.next()) {
                log.debug("Database health check: OK");
                return true;
            }
        } catch (SQLException e) {
            log.error("Database health check failed", e);
        }
        return false;
    }

    /**
     * Get database connection information
     *
     * @return Database product name and version
     */
    public String getDatabaseInfo() {
        try (Connection connection = dataSource.getConnection()) {
            String productName = connection.getMetaData().getDatabaseProductName();
            String productVersion = connection.getMetaData().getDatabaseProductVersion();
            return String.format("%s %s", productName, productVersion);
        } catch (SQLException e) {
            log.error("Failed to get database info", e);
            return "Unknown";
        }
    }

    /**
     * Test the connection pool
     * Gets multiple connections to verify pooling is working
     */
    public void testConnectionPool() throws SQLException {
        log.info("Testing connection pool...");

        // Get multiple connections - should be fast because they're pooled
        for (int i = 0; i < 5; i++) {
            long start = System.currentTimeMillis();

            try (Connection connection = dataSource.getConnection()) {
                long duration = System.currentTimeMillis() - start;
                log.info("Connection {} obtained in {}ms", i + 1, duration);

                // Verify connection is valid
                if (!connection.isValid(1)) {
                    throw new SQLException("Connection is not valid");
                }
            }
        }

        log.info("Connection pool test completed successfully");
    }
}
