package com.ocbs.ocbs.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class BootstrapConfig {

    @Value("${oscbs.bootstrap.username}")
    private String username;

    @Value("${oscbs.bootstrap.email}")
    private String email;

    @Value("${oscbs.bootstrap.password}")
    private String password;
}