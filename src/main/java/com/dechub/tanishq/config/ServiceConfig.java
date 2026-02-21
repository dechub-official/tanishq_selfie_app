package com.dechub.tanishq.config;

import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for core service beans
 * All MySQL JPA services are now auto-discovered via @Service annotations
 */
@Configuration
public class ServiceConfig {

    // All services are now implemented with MySQL JPA and auto-discovered by Spring
    // No manual bean definitions needed for the migrated services

}
