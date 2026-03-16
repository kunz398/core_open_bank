package com.ocbs.ocbs.modules.auth.service;
import com.ocbs.ocbs.modules.auth.entity.AuditLog;
import com.ocbs.ocbs.modules.auth.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.ocbs.ocbs.modules.auth.repository.AuditLogRepository;
import com.ocbs.ocbs.modules.auth.dto.AuditContext;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogService {
    private final AuditLogRepository auditLogRepository;
    /**
     * Core audit method — everything flows through here.
     *
     * @Async        — audit writes happen in background, never slow down the main request
     * Propagation.REQUIRES_NEW — audit gets its OWN transaction
     *               so if the main transaction fails, audit still saves
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(AuditContext context) {
        try {
            AuditLog entry = AuditLog.builder()
                    .userId(context.getUserId())
                    .username(context.getUsername())
                    .ipAddress(context.getIpAddress())
                    .userAgent(context.getUserAgent())
                    .action(context.getAction())
                    .resource(context.getResource())
                    .resourceId(context.getResourceId())
                    .oldValue(context.getOldValue())
                    .newValue(context.getNewValue())
                    .channel(context.getChannel())
                    .httpMethod(context.getHttpMethod())
                    .endpoint(context.getEndpoint())
                    .sessionId(context.getSessionId())
                    .correlationId(context.getCorrelationId())
                    .module("AUTH")
                    .status(context.getStatus())
                    .failureReason(context.getFailureReason())
                    .severity(context.getSeverity())
                    .metadata(context.getMetadata())
                    .build();

            auditLogRepository.save(entry);

        } catch (Exception e) {
            // audit must NEVER crash the application
            // if audit fails, log it but continue
            log.error("AUDIT WRITE FAILED — action={} user={} reason={}",
                    context.getAction(), context.getUsername(), e.getMessage());
        }
    }

    // -- Convenience methods ------------------------------------
    public void logUserCreated(UUID actorId, String actorUsername,
                               UUID newUserId, String newUsername,
                               String ip) {
        log(AuditContext.builder()
                .userId(actorId)
                .username(actorUsername)
                .action("USER_CREATED")
                .resource("USER")
                .resourceId(newUserId != null ? newUserId.toString() : null)
                .status("SUCCESS")
                .severity("INFO")
                .channel("SYSTEM")
                .ipAddress(ip)
                .newValue(Map.of("username", newUsername))
                .build());
    }

    public void logUserLocked(UUID actorId, String actorUsername,
                              UUID targetUserId, String reason,
                              String ip) {
        log(AuditContext.builder()
                .userId(actorId)
                .username(actorUsername)
                .action("USER_LOCKED")
                .resource("USER")
                .resourceId(targetUserId.toString())
                .status("SUCCESS")
                .severity("WARN")
                .channel("SYSTEM")
                .ipAddress(ip)
                .newValue(Map.of("reason", reason))
                .build());
    }

    public void logUserUnlocked(UUID actorId, String actorUsername,
                                UUID targetUserId, String ip) {
        log(AuditContext.builder()
                .userId(actorId)
                .username(actorUsername)
                .action("USER_UNLOCKED")
                .resource("USER")
                .resourceId(targetUserId.toString())
                .status("SUCCESS")
                .severity("INFO")
                .channel("SYSTEM")
                .ipAddress(ip)
                .build());
    }

    public void logPasswordChanged(UUID userId, String username, String ip) {
        log(AuditContext.builder()
                .userId(userId)
                .username(username)
                .action("PASSWORD_CHANGED")
                .resource("USER")
                .resourceId(userId.toString())
                .status("SUCCESS")
                .severity("INFO")
                .channel("SYSTEM")
                .ipAddress(ip)
                .build());
    }

    public void logFailure(String action, String username,
                           String reason, String ip) {
        log(AuditContext.builder()
                .username(username)
                .action(action)
                .resource("USER")
                .status("FAILURE")
                .severity("WARN")
                .channel("SYSTEM")
                .ipAddress(ip)
                .failureReason(reason)
                .build());
    }
}
