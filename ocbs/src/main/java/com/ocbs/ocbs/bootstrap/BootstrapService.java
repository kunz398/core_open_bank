package com.ocbs.ocbs.bootstrap;

import com.ocbs.ocbs.config.BootstrapConfig;
import com.ocbs.ocbs.modules.auth.entity.User;
import com.ocbs.ocbs.modules.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BootstrapService implements ApplicationRunner {

    /**
     * ApplicationRunner means Spring calls run() automatically
     * after the entire application has started — DB is ready,
     * all beans are loaded, security is configured.
     *
     * BootstrapService
     *   --uses--▶ BootstrapConfig  (reads credentials from env)
     *   --uses--▶ UserService      (creates the user)
     */
    private final BootstrapConfig bootstrapConfig;
    private final UserService userService;

    @Override
    public void run(ApplicationArguments args) {
        log.info("-------------------------------------------------");
        log.info("  OSCBS BOOTSTRAP CHECK");
        log.info("-------------------------------------------------");

        try {
            // -- Step 1: Check if real users already exist ------
            if (!userService.hasNoUsers()) {
                log.info("  Bootstrap already complete — skipping");
                log.info("-------------------------------------------------");
                return;
            }

            // -- Step 2: No users found — create first admin ----
            log.info("  No users found — creating first admin...");

            User admin = userService.createUser(
                    bootstrapConfig.getUsername(),
                    bootstrapConfig.getEmail(),
                    bootstrapConfig.getPassword(),
                    "Admin",
                    "User",
                    null,
                    // system user creates the first admin
                    java.util.UUID.fromString("00000000-0000-0000-0000-000000000001")
            );

            // -- Step 3: Log success — never log the password ---
            log.info("-------------------------------------------------");
            log.info("  FIRST ADMIN CREATED SUCCESSFULLY");
            log.info("-------------------------------------------------");
            log.info("  ID       : {}", admin.getId());
            log.info("  Username : {}", admin.getUsername());
            log.info("  Email    : {}", admin.getEmail());
            log.info("  Password : *** (set via BOOTSTRAP_PASSWORD env var)");
            log.warn("  * Password change required on first login");
            log.info("-------------------------------------------------");

        } catch (Exception e) {
            // -- Step 4: Bootstrap failed — app should not start -
            log.error("-------------------------------------------------");
            log.error("  BOOTSTRAP FAILED");
            log.error("  Reason : {}", e.getMessage());
            log.error("  Check your BOOTSTRAP_* environment variables");
            log.error("-------------------------------------------------");
            throw new RuntimeException("Bootstrap failed — " + e.getMessage());
        }
    }
}