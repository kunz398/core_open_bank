package com.ocbs.ocbs.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    // App
    @Value("${app.name}")
    private String appName;

    @Value("${app.short-name}")
    private String shortAppName;

    // Server
    @Value("${server.port:8080}")
    private int serverPort;

    @Value("${server.address:localhost}")
    private String serverAddress;

    @Value("${server.servlet.context-path:/}")
    private String serverContextPath;

    // Database
    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    // SMTP
    @Value("${spring.mail.host}")
    private String smtpHost;

    @Value("${spring.mail.port:587}")
    private int smtpPort;

    @Value("${spring.mail.from}")
    private String smtpFrom;

    @Value("${spring.profiles.active:}")
    private String activeProfile;

    // --- Getters ---

    public String getAppName() { return appName; }
    public String getShortAppName() { return shortAppName; }
    public int getServerPort() { return serverPort; }
    public String getServerAddress() { return serverAddress; }
    public String getServerContextPath() { return serverContextPath; }
    public String getDbUrl() { return dbUrl; }
    public String getDbUsername() { return dbUsername; }
    public String getSmtpHost() { return smtpHost; }
    public int getSmtpPort() { return smtpPort; }
    public String getSmtpFrom() { return smtpFrom; }
    public String getActiveProfile() { return activeProfile; }
}
