package com.ocbs.ocbs.modules.auth.service;
import com.ocbs.ocbs.config.PasswordPolicyConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordPolicyService {

    private final PasswordPolicyConfig policy;

    /**
     * Returns a list of violations.
     * Empty list means password passed all checks.
     */
    public List<String> validate(String password, String username)
    {
        List<String> violations = new ArrayList<>();

        if (password == null || password.isBlank()) {
            violations.add("Password must not be empty");
            return violations;
        }

        //--- Length    ---------------------------
        if (password.length() < policy.getMinLength()) {
            violations.add("Password must be at least " + policy.getMinLength() + " characters long");
        }

        if (password.length() > policy.getMaxLength()) {
            violations.add("Password must not exceed " + policy.getMaxLength() + " characters");
        }
        // -- Complexity --------------------
        if (policy.isRequireUppercase()   && password.chars().noneMatch(Character::isUpperCase)) {
            violations.add("Password must contain at least one uppercase letter");
        }

        if (policy.isRequireLowercase() && password.chars().noneMatch(Character::isLowerCase)) {
            violations.add("Password must contain at least one lowercase letter");
        }

        if (policy.isRequireDigit()  && password.chars().noneMatch(Character::isDigit)) {
            violations.add("Password must contain at least one number");
        }

        if (policy.isRequireSpecial() && !hasSpecialCharacter(password)) {
            violations.add("Password must contain at least one special character (!@#$%^&*...)");
        }

        //Cannot contain username
        if (username != null  && !username.isBlank() && password.toLowerCase().contains(username.toLowerCase())) {
            violations.add("Password must not contain your username");
        }

        if (!violations.isEmpty()) {
            log.warn("Password policy violations for '{}': {}", username, violations);
        }
        return violations;
    }

    /**
     * Throws IllegalArgumentException if password fails policy.
     * Use this in service layer for clean error handling.
     */
    public void validateOrThrow(String password, String username) {
        List<String> violations = validate(password, username);
        if (!violations.isEmpty()) {
            throw new IllegalArgumentException(
                    "Password does not meet requirements: " + violations);
        }
    }
    /**
     * Checks if password has expired based on policy config.
     * Returns false if expiry is disabled (expiryDays = 0).
     */
    public boolean isPasswordExpired(java.time.OffsetDateTime passwordChangedAt) {
        if (!policy.isPasswordExpiryEnabled()) {
            return false;
        }
        if (passwordChangedAt == null) {
            return true;
        }
        return java.time.OffsetDateTime.now().isAfter(passwordChangedAt.plusDays(policy.getExpiryDays()));
    }

    private boolean hasSpecialCharacter(String password) {
        String specialChars = "!@#$%^&*()_+-=[]{}|;':\",./<>?";
        return password.chars().anyMatch(c -> specialChars.indexOf(c) >= 0);
    }
}
