package com.ocbs.ocbs.modules.auth.service;

import com.ocbs.ocbs.config.PasswordPolicyConfig;
import com.ocbs.ocbs.modules.auth.dto.AuditContext;
import com.ocbs.ocbs.modules.auth.entity.User;
import com.ocbs.ocbs.modules.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ocbs.ocbs.modules.auth.service.AuditLogService;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordPolicyService passwordPolicyService;
    private final PasswordPolicyConfig passwordPolicyConfig;
    private final AuditLogService auditLogService;
    // --   Create User ----------------------------------------------------------
    @Transactional
    public User createUser(String username, String email, String rawPassword, String firstName, String lastName, String phoneNumber, UUID createdBy)
    {
        log.info("Attempting to create user: username={} email={}", username, email);

        // 1. validate password against policy
        passwordPolicyService.validateOrThrow(rawPassword, username);

        // 2. check username not taken
        if (userRepository.existsByUsername(username)) {
//            log.warn("User creation failed - username taken: {}", username);
            auditLogService.logFailure(                     // ← audit failed attempt
                    "USER_CREATED",
                    username,
                    "Username already taken",
                    null);
            throw new IllegalArgumentException("Username is already taken");
        }
        //3 check email not taken
        if (userRepository.existsByEmail(email)) {
//            log.warn("User creation failed - email taken: {}", email);
            auditLogService.logFailure(                     // ← audit failed attempt
                    "USER_CREATED",
                    username,
                    "Email already registered",
                    null);
            throw new IllegalArgumentException("Email is already registered");
        }

        //4 hash password
        String passwordHash = passwordEncoder.encode(rawPassword);

        //5 build and save user
        User user = User.builder()
                .username(username)
                .email(email)
                .passwordHash(passwordHash)
                .firstName(firstName)
                .lastName(lastName)
                .phoneNumber(phoneNumber)
                .isActive(true)
                .isLocked(false)
                .isMfaEnabled(false)
                .failedLoginAttempts(0)
                .mustChangePassword(true)
                .passwordChangedAt(OffsetDateTime.now())
                .createdBy(createdBy)
                .updatedBy(createdBy)
                .build();

        User savedUser = userRepository.save(user);
//        log.info("User created successfully: id={} username={}", savedUser.getId(), savedUser.getUsername());
        auditLogService.logUserCreated(
                createdBy,
                "system",
                savedUser.getId(),
                savedUser.getUsername(),
                null);
        return savedUser;
    }//end of create user

    // Get User

        @Transactional(readOnly = true)
        public User getUserById(UUID id)
        {
        log.info("Fetching user by id: {}", id);
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User not found: id={}", id);
                    return new RuntimeException("User not found: " + id);
                });
    }


    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        UUID systemId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        return userRepository.findAllExcludingSystem(systemId);
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsersIncludingSystem() {
        log.info("Fetching all users including system");
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
        public User getUserByUsername(String username)
        {
        log.info("Fetching user by username: {}", username);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User not found: username={}", username);
                    return new RuntimeException("User not found: " + username);
                });
        }
    @Transactional(readOnly = true)
    public User getUserByEmail(String email)
    {
        log.info("Fetching user by Email: {}", email);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User not found: email={}", email);
                    return new RuntimeException("User not found: " + email);
                });
    }


    // lock / unlock
    @Transactional
    public void lockUser(UUID id, String reason)
    {
//        log.warn("Locking user: id={} reason={}", id, reason);

        // make sure user exists first
        getUserById(id);

        userRepository.lockUser(id, OffsetDateTime.now(), reason);
        auditLogService.logUserLocked(null, "system", id, reason, null);  // ← audit
        //        log.info("User locked: id={}", id);
    }

    @Transactional
    public void unlockUser(UUID id)
    {
//        log.info("Unlocking user: id={}", id);

        // make sure user exists first
        getUserById(id);

        userRepository.unlockUser(id);
        auditLogService.logUserUnlocked(null, "system", id, null);        // ← audit

        //        log.info("User unlocked: id={}", id);
    }

    //Password
    @Transactional
    public void changePassword(UUID id, String rawNewPassword) {
        log.info("Changing password for user: id={}", id);

        User user = getUserById(id);

        // validate new password against policy
        passwordPolicyService.validateOrThrow(rawNewPassword, user.getUsername());

        // make sure new password is different from current
        if (passwordEncoder.matches(rawNewPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException(
                    "New password must be different from current password");
        }

        String newHash = passwordEncoder.encode(rawNewPassword);
        userRepository.updatePassword(id, newHash, OffsetDateTime.now());
        auditLogService.logPasswordChanged(id, user.getUsername(), null); // ← audit
//        log.info("Password changed successfully for user: id={}", id);
    }


    // Password expiry check
    @Transactional(readOnly = true)
    public boolean isPasswordExpired(UUID id) {
        User user = getUserById(id);
        return passwordPolicyService.isPasswordExpired(user.getPasswordChangedAt());
    }

    // bootstrap helper
    @Transactional(readOnly = true)
    public boolean hasNoUsers() {
        UUID systemId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        return userRepository.countNonSystemUsers(systemId) == 0;
    }

        // -- Update User------------------
    @Transactional
    public User updateUser(UUID id, String firstName,
                           String lastName, String phoneNumber) {
        User user = getUserById(id);

        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPhoneNumber(phoneNumber);
        user.setUpdatedBy(UUID.fromString("00000000-0000-0000-0000-000000000001"));

        User updatedUser = userRepository.save(user);

        auditLogService.log(AuditContext.builder()
                .userId(updatedUser.getId())
                .username(updatedUser.getUsername())
                .action("USER_UPDATED")
                .resource("USER")
                .resourceId(id.toString())
                .status("SUCCESS")
                .severity("INFO")
                .channel("SYSTEM")
                .oldValue(Map.of(
                        "firstName", user.getFirstName() != null ? user.getFirstName() : "",
                        "lastName",  user.getLastName()  != null ? user.getLastName()  : "",
                        "phone",     user.getPhoneNumber() != null ? user.getPhoneNumber() : ""
                ))
                .newValue(Map.of(
                        "firstName", firstName != null ? firstName : "",
                        "lastName",  lastName  != null ? lastName  : "",
                        "phone",     phoneNumber != null ? phoneNumber : ""
                ))
                .build());

        log.info("User updated: id={}", id);
        return updatedUser;
    }
// Deactivate Use -------------
@Transactional
public void deactivateUser(UUID id) {
    User user = getUserById(id);

    user.setIsActive(false);
    user.setUpdatedBy(UUID.fromString("00000000-0000-0000-0000-000000000001"));
    userRepository.save(user);

    auditLogService.log(AuditContext.builder()
            .userId(id)
            .username(user.getUsername())
            .action("USER_DEACTIVATED")
            .resource("USER")
            .resourceId(id.toString())
            .status("SUCCESS")
            .severity("WARN")
            .channel("SYSTEM")
            .build());

    log.info("User deactivated: id={}", id);
}

}//end of class
