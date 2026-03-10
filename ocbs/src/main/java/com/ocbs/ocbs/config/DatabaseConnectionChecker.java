package com.ocbs.ocbs.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component
@Profile("dev")
public class DatabaseConnectionChecker {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnectionChecker.class);

    private final DataSource dataSource;

    public DatabaseConnectionChecker(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void checkDatabaseConnection() {
        try (Connection connection = dataSource.getConnection()) {
            String dbUrl = connection.getMetaData().getURL();
            String dbProduct = connection.getMetaData().getDatabaseProductName();
            String dbVersion = connection.getMetaData().getDatabaseProductVersion();
            logger.info("Database connection successful!");
            logger.info("  URL     : {}", dbUrl);
            logger.info("  Product : {} {}", dbProduct, dbVersion);
        } catch (SQLException e) {
            logger.error("Database connection failed: {}", e.getMessage());
        }
    }
}
