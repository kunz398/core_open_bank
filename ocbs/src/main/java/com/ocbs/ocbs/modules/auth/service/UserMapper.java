package com.ocbs.ocbs.modules.auth.service;

import com.ocbs.ocbs.modules.auth.dto.response.UserResponse;
import com.ocbs.ocbs.modules.auth.entity.User;
import org.springframework.stereotype.Component;
import java.util.List;
@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .isActive(user.getIsActive())
                .isLocked(user.getIsLocked())
                .mustChangePassword(user.getMustChangePassword())
                .createdAt(user.getCreatedAt())
                .build();
    }

    public List<UserResponse> toResponseList(List<User> users)
    {
        return users.stream()
                .map(this::toResponse)
                .toList();
    }

}