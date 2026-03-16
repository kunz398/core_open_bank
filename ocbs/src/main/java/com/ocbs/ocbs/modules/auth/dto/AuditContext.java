package com.ocbs.ocbs.modules.auth.dto;

import lombok.*;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditContext {
    private UUID userId;
    private String username;
    private String ipAddress;
    private String userAgent;

    private String action;
    private String resource;
    private String resourceId;

    private Map<String, Object> oldValue;
    private Map<String, Object> newValue;

    private String channel;
    private String httpMethod;
    private String endpoint;

    private UUID sessionId;
    private UUID correlationId;

    private String status;
    private String failureReason;

    @Builder.Default
    private String severity = "INFO";

    private Map<String, Object> metadata;
}
