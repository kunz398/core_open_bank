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


import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;
import com.zaxxer.hikari.HikariPoolMXBean;
import com.zaxxer.hikari.HikariConfigMXBean;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import java.util.Arrays;
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

    private final Environment environment;

    public DatabaseConnectionChecker(DataSource dataSource, Environment environment) {
        this.dataSource = dataSource;
        this.environment = environment;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Order(1)                          // runs first
    public void checkActiveProfile() {
        String profile = Arrays.stream(environment.getActiveProfiles())
                .findFirst()
                .orElse("default")
                .toUpperCase();

        switch (profile) {
            case "PROD" -> {
                logger.warn("┌─────────────────────────────────────────────────┐");
                logger.warn("│                                                 │");
                logger.warn("│          ⚠   PRODUCTION ENVIRONMENT  ⚠         │");
                logger.warn("│                                                 │");
                logger.warn("│      ALL ACTIONS ARE LIVE AND IRREVERSIBLE      │");
                logger.warn("│         VERIFY ALL CHANGES BEFORE DEPLOY        │");
                logger.warn("│                                                 │");
                logger.warn("└─────────────────────────────────────────────────┘");
            }
            case "DEV" -> {
                logger.info("┌─────────────────────────────────────────────────┐");
                logger.info("│                                                 │");
                logger.info("│           DEVELOPMENT  ENVIRONMENT              │");
                logger.info("│                                                 │");
                logger.info("│          Safe to test — not production          │");
                logger.info("│                                                 │");
                logger.info("└─────────────────────────────────────────────────┘");
            }
            default -> {
                logger.warn("┌─────────────────────────────────────────────────┐");
                logger.warn("│                                                 │");
                logger.warn("│         UNKNOWN ENVIRONMENT : {}             │", profile);
                logger.warn("│                                                 │");
                logger.warn("│         Verify your active Spring profile       │");
                logger.warn("│                                                 │");
                logger.warn("└─────────────────────────────────────────────────┘");
            }
        }
    }
    @EventListener(ApplicationReadyEvent.class)
    @Order(2)
    public void checkDatabaseConnection() {
        try (Connection connection = dataSource.getConnection()) {

            // DB metadata
            var meta = connection.getMetaData();

            // Cast to HikariDataSource to get pool stats
            HikariDataSource hikariDS = (HikariDataSource) dataSource;
            HikariPoolMXBean poolBean = hikariDS.getHikariPoolMXBean();
            HikariConfigMXBean configBean = hikariDS.getHikariConfigMXBean();

            logger.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            logger.info("  DATABASE CONNECTION");
            logger.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            logger.info("  Status    : CONNECTED ✓");
            logger.info("  URL       : {}", meta.getURL());
            logger.info("  Database  : {} v{}", meta.getDatabaseProductName(), meta.getDatabaseProductVersion());
            logger.info("  Driver    : {} v{}", meta.getDriverName(), meta.getDriverVersion());
            logger.info("  Schema    : {}", connection.getSchema());
            logger.info("  Catalog   : {}", connection.getCatalog());
            logger.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            logger.info("  HIKARICP POOL — {}", configBean.getPoolName());
            logger.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            logger.info("  Pool Size (max)    : {}", configBean.getMaximumPoolSize());
            logger.info("  Min Idle           : {}", configBean.getMinimumIdle());
            logger.info("  Conn Timeout (ms)  : {}", configBean.getConnectionTimeout());
            logger.info("  Idle Timeout (ms)  : {}", configBean.getIdleTimeout());
            logger.info("  Max Lifetime (ms)  : {}", configBean.getMaxLifetime());
            logger.info("─────────────────────────────────────────────────");
            logger.info("  Active Connections : {}", poolBean.getActiveConnections());
            logger.info("  Idle Connections   : {}", poolBean.getIdleConnections());
            logger.info("  Total Connections  : {}", poolBean.getTotalConnections());
            logger.info("  Threads Awaiting   : {}", poolBean.getThreadsAwaitingConnection());
            logger.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        } catch (SQLException e) {
            logger.error("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            logger.error("  DATABASE CONNECTION FAILED");
            logger.error("  Reason : {}", e.getMessage());
            logger.error("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    @Order(3)
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
