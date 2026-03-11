package com.ocbs.ocbs.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@Profile("dev")
public class DatabaseConnectionChecker {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnectionChecker.class);

    private final DataSource dataSource;

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Value("${spring.datasource.username}")
    private String datasourceUsername;

    @Value("${spring.datasource.password}")
    private String datasourcePassword;

    @Value("${spring.database.backup:false}")
    private boolean backupEnabled;

    @Value("${spring.database.backup.path:db_backups}")
    private String backupDir;

    @Value("${spring.database.backup.pgdump-path:pg_dump}")
    private String pgDumpPath;

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

    @EventListener(ApplicationReadyEvent.class)
    public void backupDatabase() {
        if (!backupEnabled) {
            logger.info("Database backup is disabled (spring.database.backup=false)");
            return;
        }

        // Extract database name from JDBC URL: jdbc:postgresql://host:port/dbname
        String dbName = datasourceUrl.substring(datasourceUrl.lastIndexOf('/') + 1);

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        String fileName = dbName + "_" + timestamp + ".sql";

        try {
            Path backupPath = Paths.get(backupDir);
            Files.createDirectories(backupPath);
            Path outputFile = backupPath.resolve(fileName);

            logger.info("Starting database backup: {}", outputFile);

            // pg_dump uses PGPASSWORD env variable to avoid interactive password prompt
            ProcessBuilder pb = new ProcessBuilder(
                    pgDumpPath,
                    "-h", extractHost(datasourceUrl),
                    "-p", extractPort(datasourceUrl),
                    "-U", datasourceUsername,
                    "-F", "p",           // plain SQL format
                    "-f", outputFile.toString(),
                    dbName
            );
            pb.environment().put("PGPASSWORD", datasourcePassword);
            pb.redirectErrorStream(true);

            Process process = pb.start();
            String processOutput = new String(process.getInputStream().readAllBytes()).trim();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                long fileSizeKb = Files.size(outputFile) / 1024;
                logger.info("Database backup completed: {} ({} KB)", outputFile, fileSizeKb);
            } else {
                logger.error("Database backup failed (exit {}): {}",
                        exitCode, processOutput.isEmpty() ? "(no output from pg_dump)" : processOutput);
            }
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Database backup failed: {}", e.getMessage());
        }
    }

    /** Extracts host from jdbc:postgresql://host:port/dbname */
    private String extractHost(String url) {
        // e.g. jdbc:postgresql://localhost:5432/oscbs
        String withoutScheme = url.substring(url.indexOf("//") + 2); // localhost:5432/oscbs
        return withoutScheme.substring(0, withoutScheme.indexOf(':'));
    }

    /** Extracts port from jdbc:postgresql://host:port/dbname */
    private String extractPort(String url) {
        String withoutScheme = url.substring(url.indexOf("//") + 2); // localhost:5432/oscbs
        String hostPort = withoutScheme.substring(0, withoutScheme.indexOf('/'));
        return hostPort.contains(":") ? hostPort.substring(hostPort.indexOf(':') + 1) : "5432";
    }

}
