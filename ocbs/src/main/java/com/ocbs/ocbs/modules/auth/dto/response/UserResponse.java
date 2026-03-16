package com.ocbs.ocbs.modules.auth.dto.response;


import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private UUID id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private Boolean isActive;
    private Boolean isLocked;
    private Boolean mustChangePassword;
    private OffsetDateTime createdAt;
}