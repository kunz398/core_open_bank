package com.ocbs.ocbs.modules.auth.service;


import com.ocbs.ocbs.config.JwtService;
import com.ocbs.ocbs.modules.auth.dto.AuditContext;
import com.ocbs.ocbs.modules.auth.dto.request.LoginRequest;
import com.ocbs.ocbs.modules.auth.dto.response.LoginResponse;
import com.ocbs.ocbs.modules.auth.entity.User;
import com.ocbs.ocbs.modules.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.*;

import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final AuditLogService auditLogService;

    @Transactional
    public LoginResponse login(LoginRequest request, String ipAddress)
    {
        // find user by username or email
        User user = userRepository
                .findByUsernameOrEmail(
                        request.getUsernameOrEmail(),
                        request.getUsernameOrEmail())
                .orElseThrow(() -> {
                    auditLogService.logFailure(
                            "LOGIN_FAILED",
                            request.getUsernameOrEmail(),
                            "User not found",
                            ipAddress);
                    // intentionally vague — don't reveal if user exists
                    return new RuntimeException("Invalid credentials");
                });
        // 2. check account is active
        if (!user.getIsActive()) {
            auditLogService.logFailure(
                    "LOGIN_FAILED",
                    user.getUsername(),
                    "Account is inactive",
                    ipAddress);
            throw new RuntimeException("Account is inactive");
        }

        // 3. check account is not locked
        if (user.getIsLocked()) {
            auditLogService.logFailure(
                    "LOGIN_FAILED",
                    user.getUsername(),
                    "Account is locked: " + user.getLockReason(),
                    ipAddress);
            throw new RuntimeException("Account is locked");
        }

        // 4. verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            // increment failed attempts
            userRepository.incrementFailedLoginAttempts(user.getId());

            auditLogService.logFailure(
                    "LOGIN_FAILED",
                    user.getUsername(),
                    "Invalid password",
                    ipAddress);
            throw new RuntimeException("Invalid credentials");
        }

        // 5. record successful login
        userRepository.recordSuccessfulLogin(
                user.getId(),
                OffsetDateTime.now(),
                ipAddress);

        // 6. get roles as comma separated string
        // hardcoded for now — will come from user_roles table after we wire roles
        String roles = "ADMIN";

        // 7. generate tokens
        String accessToken  = jwtService.generateAccessToken(
                user.getId(), user.getUsername(), roles);
        String refreshToken = jwtService.generateRefreshToken(user.getId());

        // 8. audit success
        auditLogService.log(com.ocbs.ocbs.modules.auth.dto.AuditContext.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .action("LOGIN_SUCCESS")
                .resource("USER")
                .resourceId(user.getId().toString())
                .status("SUCCESS")
                .severity("INFO")
                .channel("WEB")
                .ipAddress(ipAddress)
                .build());

        log.info("Login successful: username={} ip={}", user.getUsername(), ipAddress);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(900000)
                .user(userMapper.toResponse(user))
                .build();

    }

}
