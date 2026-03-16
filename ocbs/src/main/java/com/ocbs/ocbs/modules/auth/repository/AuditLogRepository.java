package com.ocbs.ocbs.modules.auth.repository;

import com.ocbs.ocbs.modules.auth.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    // find all audit entries for a specific user
    List<AuditLog> findByUserIdOrderByCreatedAtDesc(UUID userId);

    // find all entries for a specific action
    List<AuditLog> findByActionOrderByCreatedAtDesc(String action);

    // find all entries for a specific resource
    List<AuditLog> findByResourceAndResourceIdOrderByCreatedAtDesc(
            String resource, String resourceId);
}