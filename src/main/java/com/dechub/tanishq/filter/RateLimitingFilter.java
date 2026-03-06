package com.dechub.tanishq.filter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate Limiting Filter to prevent abuse of form submission endpoints
 * Implements OWASP A07 (Security Misconfiguration) - Request Throttling
 *
 * Rate Limit: 10 requests per minute per IP address
 * Returns HTTP 429 (Too Many Requests) when limit is exceeded
 *
 * NOTE: This filter is registered via RateLimitingConfig.java using @Bean annotation
 * Do not add @Component annotation to avoid duplicate bean definition
 */
public class RateLimitingFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitingFilter.class);

    // Rate limit configuration
    private static final int REQUESTS_PER_MINUTE = 10;
    private static final int REFILL_TOKENS = 10;

    // Store buckets per IP address
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    // Endpoints that require rate limiting (form submission endpoints)
    private static final String[] RATE_LIMITED_ENDPOINTS = {
        "/events/login",
        "/events/abm_login",
        "/events/rbm_login",
        "/events/cee_login",
        "/events/corporate_login",
        "/events/upload",
        "/events/attendees",
        "/events/uploadCompletedEvents",
        "/events/changePassword",
        "/events/updateSaleOfAnEvent",
        "/events/updateAdvanceOfAnEvent",
        "/events/updateGhsRgaOfAnEvent",
        "/events/updateGmbOfAnEvent"
    };

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("Rate Limiting Filter initialized - {} requests per minute per IP", REQUESTS_PER_MINUTE);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String requestURI = request.getRequestURI();

        // Check if this endpoint requires rate limiting
        if (shouldApplyRateLimit(requestURI)) {
            String clientIP = getClientIP(request);
            Bucket bucket = resolveBucket(clientIP);

            // Try to consume 1 token from the bucket
            if (bucket.tryConsume(1)) {
                // Token consumed successfully, allow the request
                log.debug("Rate limit check passed for IP: {} on endpoint: {}", clientIP, requestURI);
                filterChain.doFilter(servletRequest, servletResponse);
            } else {
                // Rate limit exceeded
                log.warn("Rate limit exceeded for IP: {} on endpoint: {}", clientIP, requestURI);
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(
                    "{\"success\": false, \"message\": \"Too many requests. Please try again later.\", \"error\": \"RATE_LIMIT_EXCEEDED\"}"
                );
            }
        } else {
            // No rate limiting needed for this endpoint
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    @Override
    public void destroy() {
        // Clear all buckets on filter destruction
        buckets.clear();
        log.info("Rate Limiting Filter destroyed");
    }

    /**
     * Check if rate limiting should be applied to this endpoint
     */
    private boolean shouldApplyRateLimit(String requestURI) {
        if (requestURI == null) {
            return false;
        }

        for (String endpoint : RATE_LIMITED_ENDPOINTS) {
            if (requestURI.equals(endpoint)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Resolve or create a bucket for the given IP address
     */
    private Bucket resolveBucket(String clientIP) {
        return buckets.computeIfAbsent(clientIP, k -> createNewBucket());
    }

    /**
     * Create a new bucket with rate limit configuration
     * 10 requests per minute with greedy refill
     */
    private Bucket createNewBucket() {
        Bandwidth limit = Bandwidth.classic(
            REQUESTS_PER_MINUTE,
            Refill.greedy(REFILL_TOKENS, Duration.ofMinutes(1))
        );
        return Bucket.builder()
            .addLimit(limit)
            .build();
    }

    /**
     * Extract client IP address from request
     * Handles proxy headers (X-Forwarded-For, X-Real-IP)
     */
    private String getClientIP(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // Handle multiple IPs in X-Forwarded-For (take the first one)
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip != null ? ip : "UNKNOWN";
    }
}

