package com.dechub.tanishq.util;

import com.dechub.tanishq.entity.*;
import com.dechub.tanishq.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Password Migration Utility
 *
 * Bulk migrates plain text passwords to BCrypt hashed passwords
 *
 * USAGE:
 * This utility runs automatically on application startup ONLY when the
 * "migrate-passwords" profile is active.
 *
 * To run password migration:
 * java -jar tanishq.war --spring.profiles.active=migrate-passwords,preprod
 *
 * IMPORTANT:
 * - Run during maintenance window (off-hours)
 * - Database backups MUST be taken before running
 * - Test on UAT environment first
 * - Monitor logs for any errors
 *
 * Security Fix: OWASP A02 - Cryptographic Failures
 *
 * @author Security Team
 * @since March 2026
 */
@Component
@Profile("migrate-passwords") // Only runs when this profile is active
public class PasswordMigrationUtility implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(PasswordMigrationUtility.class);

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AbmLoginRepository abmLoginRepository;

    @Autowired
    private RbmLoginRepository rbmLoginRepository;

    @Autowired
    private CeeLoginRepository ceeLoginRepository;

    @Autowired
    private CorporateLoginRepository corporateLoginRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        log.info("==============================================");
        log.info("PASSWORD MIGRATION UTILITY - STARTING");
        log.info("==============================================");

        AtomicInteger totalMigrated = new AtomicInteger(0);
        AtomicInteger totalSkipped = new AtomicInteger(0);
        AtomicInteger totalErrors = new AtomicInteger(0);

        try {
            // Migrate ABM passwords
            log.info("Migrating ABM passwords...");
            migrateAbmPasswords(totalMigrated, totalSkipped, totalErrors);

            // Migrate RBM passwords
            log.info("Migrating RBM passwords...");
            migrateRbmPasswords(totalMigrated, totalSkipped, totalErrors);

            // Migrate CEE passwords
            log.info("Migrating CEE passwords...");
            migrateCeePasswords(totalMigrated, totalSkipped, totalErrors);

            // Migrate Corporate passwords
            log.info("Migrating Corporate passwords...");
            migrateCorporatePasswords(totalMigrated, totalSkipped, totalErrors);

            // Migrate Store User passwords
            log.info("Migrating Store User passwords...");
            migrateUserPasswords(totalMigrated, totalSkipped, totalErrors);

            log.info("==============================================");
            log.info("PASSWORD MIGRATION COMPLETED");
            log.info("Total Migrated: {}", totalMigrated.get());
            log.info("Total Skipped (already hashed): {}", totalSkipped.get());
            log.info("Total Errors: {}", totalErrors.get());
            log.info("==============================================");

        } catch (Exception e) {
            log.error("FATAL ERROR during password migration", e);
            throw e;
        }
    }

    /**
     * Check if password is already BCrypt hashed
     */
    private boolean isAlreadyHashed(String password) {
        return password != null &&
               (password.startsWith("$2a$") || password.startsWith("$2b$") || password.startsWith("$2y$"));
    }

    /**
     * Migrate ABM login passwords
     */
    private void migrateAbmPasswords(AtomicInteger migrated, AtomicInteger skipped, AtomicInteger errors) {
        try {
            List<AbmLogin> users = abmLoginRepository.findAll();
            log.info("Found {} ABM users", users.size());

            for (AbmLogin user : users) {
                try {
                    String currentPassword = user.getPassword();

                    if (isAlreadyHashed(currentPassword)) {
                        skipped.incrementAndGet();
                        log.debug("ABM user {} already has hashed password, skipping", user.getAbmUserId());
                        continue;
                    }

                    // Hash the plain text password
                    String hashedPassword = passwordEncoder.encode(currentPassword);
                    user.setPassword(hashedPassword);
                    abmLoginRepository.save(user);

                    migrated.incrementAndGet();
                    log.info("Migrated ABM user: {}", user.getAbmUserId());

                } catch (Exception e) {
                    errors.incrementAndGet();
                    log.error("Error migrating ABM user: {}", user.getAbmUserId(), e);
                }
            }
        } catch (Exception e) {
            log.error("Error fetching ABM users", e);
            throw e;
        }
    }

    /**
     * Migrate RBM login passwords
     */
    private void migrateRbmPasswords(AtomicInteger migrated, AtomicInteger skipped, AtomicInteger errors) {
        try {
            List<RbmLogin> users = rbmLoginRepository.findAll();
            log.info("Found {} RBM users", users.size());

            for (RbmLogin user : users) {
                try {
                    String currentPassword = user.getPassword();

                    if (isAlreadyHashed(currentPassword)) {
                        skipped.incrementAndGet();
                        log.debug("RBM user {} already has hashed password, skipping", user.getRbmUserId());
                        continue;
                    }

                    String hashedPassword = passwordEncoder.encode(currentPassword);
                    user.setPassword(hashedPassword);
                    rbmLoginRepository.save(user);

                    migrated.incrementAndGet();
                    log.info("Migrated RBM user: {}", user.getRbmUserId());

                } catch (Exception e) {
                    errors.incrementAndGet();
                    log.error("Error migrating RBM user: {}", user.getRbmUserId(), e);
                }
            }
        } catch (Exception e) {
            log.error("Error fetching RBM users", e);
            throw e;
        }
    }

    /**
     * Migrate CEE login passwords
     */
    private void migrateCeePasswords(AtomicInteger migrated, AtomicInteger skipped, AtomicInteger errors) {
        try {
            List<CeeLogin> users = ceeLoginRepository.findAll();
            log.info("Found {} CEE users", users.size());

            for (CeeLogin user : users) {
                try {
                    String currentPassword = user.getPassword();

                    if (isAlreadyHashed(currentPassword)) {
                        skipped.incrementAndGet();
                        log.debug("CEE user {} already has hashed password, skipping", user.getCeeUserId());
                        continue;
                    }

                    String hashedPassword = passwordEncoder.encode(currentPassword);
                    user.setPassword(hashedPassword);
                    ceeLoginRepository.save(user);

                    migrated.incrementAndGet();
                    log.info("Migrated CEE user: {}", user.getCeeUserId());

                } catch (Exception e) {
                    errors.incrementAndGet();
                    log.error("Error migrating CEE user: {}", user.getCeeUserId(), e);
                }
            }
        } catch (Exception e) {
            log.error("Error fetching CEE users", e);
            throw e;
        }
    }

    /**
     * Migrate Corporate login passwords
     */
    private void migrateCorporatePasswords(AtomicInteger migrated, AtomicInteger skipped, AtomicInteger errors) {
        try {
            List<CorporateLogin> users = corporateLoginRepository.findAll();
            log.info("Found {} Corporate users", users.size());

            for (CorporateLogin user : users) {
                try {
                    String currentPassword = user.getPassword();

                    if (isAlreadyHashed(currentPassword)) {
                        skipped.incrementAndGet();
                        log.debug("Corporate user {} already has hashed password, skipping", user.getCorporateUserId());
                        continue;
                    }

                    String hashedPassword = passwordEncoder.encode(currentPassword);
                    user.setPassword(hashedPassword);
                    corporateLoginRepository.save(user);

                    migrated.incrementAndGet();
                    log.info("Migrated Corporate user: {}", user.getCorporateUserId());

                } catch (Exception e) {
                    errors.incrementAndGet();
                    log.error("Error migrating Corporate user: {}", user.getCorporateUserId(), e);
                }
            }
        } catch (Exception e) {
            log.error("Error fetching Corporate users", e);
            throw e;
        }
    }

    /**
     * Migrate Store User passwords
     */
    private void migrateUserPasswords(AtomicInteger migrated, AtomicInteger skipped, AtomicInteger errors) {
        try {
            List<User> users = userRepository.findAll();
            log.info("Found {} Store users", users.size());

            for (User user : users) {
                try {
                    String currentPassword = user.getPassword();

                    // Skip users without passwords
                    if (currentPassword == null || currentPassword.trim().isEmpty()) {
                        skipped.incrementAndGet();
                        log.debug("Store user {} has no password, skipping", user.getUsername());
                        continue;
                    }

                    if (isAlreadyHashed(currentPassword)) {
                        skipped.incrementAndGet();
                        log.debug("Store user {} already has hashed password, skipping", user.getUsername());
                        continue;
                    }

                    String hashedPassword = passwordEncoder.encode(currentPassword);
                    user.setPassword(hashedPassword);
                    userRepository.save(user);

                    migrated.incrementAndGet();
                    log.info("Migrated Store user: {}", user.getUsername());

                } catch (Exception e) {
                    errors.incrementAndGet();
                    log.error("Error migrating Store user: {}", user.getUsername(), e);
                }
            }
        } catch (Exception e) {
            log.error("Error fetching Store users", e);
            throw e;
        }
    }
}

