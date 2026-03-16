package com.ocbs.ocbs.modules.auth.controller;

import com.ocbs.ocbs.modules.auth.dto.request.CreateUserRequest;
import com.ocbs.ocbs.modules.auth.dto.response.UserResponse;
import com.ocbs.ocbs.modules.auth.entity.User;
import com.ocbs.ocbs.modules.auth.service.AuditLogService;
import com.ocbs.ocbs.modules.auth.service.UserMapper;
import com.ocbs.ocbs.modules.auth.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;
    private final AuditLogService auditLogService;

    // -- GET /api/v1/users --------------------------------------
    // Get all users
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers(
            HttpServletRequest httpRequest) {

        log.info("GET /api/v1/users — ip={}", httpRequest.getRemoteAddr());

        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(userMapper.toResponseList(users));
    }

}