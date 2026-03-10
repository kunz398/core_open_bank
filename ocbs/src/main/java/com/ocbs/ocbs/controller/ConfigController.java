package com.ocbs.ocbs.controller;

import com.ocbs.ocbs.config.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/config")
public class ConfigController {

    private static final Logger log = LoggerFactory.getLogger(ConfigController.class);

    private final AppConfig appConfig;

    public ConfigController(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    @GetMapping("/test")
    public String testConfig() {
        log.info("========== ENV CONFIG TEST ==========");
        log.info("App Name       : {}", appConfig.getAppName());
        log.info("Short Name     : {}", appConfig.getShortAppName());
        log.info("Server         : {}:{}{}", appConfig.getServerAddress(), appConfig.getServerPort(), appConfig.getServerContextPath());
        log.info("DB             : {} (user: {})", appConfig.getDbUrl(), appConfig.getDbUsername());
        log.info("SMTP           : {}:{} (from: {})", appConfig.getSmtpHost(), appConfig.getSmtpPort(), appConfig.getSmtpFrom());
        log.info("=====================================");
        return "Config loaded from .env — check the console/logs!";
    }
}
