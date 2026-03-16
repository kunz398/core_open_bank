package com.ocbs.ocbs.modules.auth.repository;
import com.ocbs.ocbs.modules.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

// Lookups
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);

    // login can be either username or email
    Optional<User> findByUsernameOrEmail(String username, String email);

    // Existence checks
    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    //  Custom queries
    // count real users excluding the system user
    @Query("SELECT COUNT(u) FROM User u WHERE u.id != :systemId")
    long countNonSystemUsers(@Param("systemId") UUID systemId);

    //Updates
    @Modifying
    @Query("UPDATE User u SET u.failedLoginAttempts = u.failedLoginAttempts + 1 WHERE u.id = :id")
    void incrementFailedLoginAttempts(@Param("id") UUID id);

    @Modifying
    @Query("UPDATE User u SET u.failedLoginAttempts = 0, u.lastLoginAt = :now, u.lastLoginIp = :ip WHERE u.id = :id")
    void recordSuccessfulLogin(@Param("id") UUID id,
                               @Param("now") OffsetDateTime now,
                               @Param("ip") String ip);

    @Modifying
    @Query("UPDATE User u SET u.isLocked = true, u.lockedAt = :now, u.lockReason = :reason WHERE u.id = :id")
    void lockUser(@Param("id") UUID id,
                  @Param("now") OffsetDateTime now,
                  @Param("reason") String reason);

    @Modifying
    @Query("UPDATE User u SET u.isLocked = false, u.lockedAt = null, u.lockReason = null, u.failedLoginAttempts = 0 WHERE u.id = :id")
    void unlockUser(@Param("id") UUID id);

    @Modifying
    @Query("UPDATE User u SET u.passwordHash = :hash, u.passwordChangedAt = :now, u.mustChangePassword = false WHERE u.id = :id")
    void updatePassword(@Param("id") UUID id,
                        @Param("hash") String hash,
                        @Param("now") OffsetDateTime now);


    @Query("SELECT u FROM User u")
    List<User> findAllExcludingSystem(@Param("systemId") UUID systemId);



}
