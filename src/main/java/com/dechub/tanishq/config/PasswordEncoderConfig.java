package com.dechub.tanishq.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Password Encoder Configuration
 * Implements BCrypt password hashing for secure password storage
 *
 * Security Fix: OWASP A02 - Cryptographic Failures
 * Vulnerability: Passwords stored in plain text in database
 * Solution: BCrypt hashing with strength 12
 *
 * @author Security Team
 * @since March 2026
 */
@Configuration
public class PasswordEncoderConfig {

    /**
     * Creates BCryptPasswordEncoder bean for password hashing
     *
     * BCrypt Features:
     * - Adaptive hashing: computation cost increases over time
     * - Automatic salt generation per password
     * - Strength 12: 2^12 = 4096 iterations (secure & performant)
     * - Industry standard for password hashing
     *
     * @return PasswordEncoder instance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        // Strength 12 is the recommended default balance between security and performance
        return new BCryptPasswordEncoder(12);
    }
}

