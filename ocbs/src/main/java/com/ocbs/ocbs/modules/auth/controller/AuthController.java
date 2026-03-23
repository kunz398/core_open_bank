package com.ocbs.ocbs.modules.auth.controller;

import com.ocbs.ocbs.modules.auth.dto.request.LoginRequest;
import com.ocbs.ocbs.modules.auth.dto.response.LoginResponse;
import com.ocbs.ocbs.modules.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * POST /api/v1/auth/login
     * Public endpoint — no token required.
     * Accepts username or email + password, returns access + refresh tokens.
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        log.info("POST /api/v1/auth/login — ip={}", httpRequest.getRemoteAddr());
        LoginResponse response = authService.login(request, httpRequest.getRemoteAddr());
        return ResponseEntity.ok(response);
    }
}
