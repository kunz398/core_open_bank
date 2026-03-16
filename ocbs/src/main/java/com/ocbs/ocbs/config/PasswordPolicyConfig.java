package com.ocbs.ocbs.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "oscbs.security.password")
public class PasswordPolicyConfig {
    private int minLength = 12;
    private int maxLength = 64;
    private boolean requireUppercase = true;
    private boolean requireLowercase = true;
    private boolean requireDigit = true;
    private boolean requireSpecial = true;
    private int historyCount = 5;
    private int expiryDays = 0;
    private int maxFailedAttempts = 5;

    public boolean isPasswordExpiryEnabled() {
        return expiryDays > 0;
    }
}
