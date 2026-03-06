package com.dechub.tanishq.config;

import com.dechub.tanishq.filter.RateLimitingFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Rate Limiting Filter
 * Implements OWASP A07 (Security Misconfiguration) - Request Throttling
 */
@Configuration
public class RateLimitingConfig {

    private static final Logger log = LoggerFactory.getLogger(RateLimitingConfig.class);

    /**
     * Register the RateLimitingFilter with the servlet container
     * Applies to all URLs, but the filter internally checks which endpoints to protect
     */
    @Bean
    public FilterRegistrationBean<RateLimitingFilter> rateLimitingFilter() {
        FilterRegistrationBean<RateLimitingFilter> registrationBean = new FilterRegistrationBean<>();

        registrationBean.setFilter(new RateLimitingFilter());
        registrationBean.addUrlPatterns("/events/*"); // Apply to all events endpoints
        registrationBean.setOrder(1); // Execute early in the filter chain
        registrationBean.setName("rateLimitingFilter");

        log.info("Rate Limiting Filter registered for /events/* endpoints");

        return registrationBean;
    }
}

