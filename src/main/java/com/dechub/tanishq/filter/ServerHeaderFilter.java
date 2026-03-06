package com.dechub.tanishq.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Server Header Filter to prevent server information disclosure
 * Implements OWASP A05 (Security Misconfiguration) - Information Disclosure Prevention
 *
 * This filter removes or masks server-related headers that could expose:
 * - Server type and version (Apache Tomcat, Jetty, etc.)
 * - Infrastructure details (AWS ELB, CloudFront)
 * - Technology stack information
 *
 * SECURITY BENEFIT:
 * - Prevents attackers from identifying server version-specific vulnerabilities
 * - Reduces information available for reconnaissance attacks
 * - Complies with security best practices for production environments
 *
 * @author Tanishq Security Team
 * @since 2026-03-05
 */
@Component
@Order(2) // Execute after RateLimitingFilter (Order 1)
public class ServerHeaderFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(ServerHeaderFilter.class);

    /**
     * Initialize the filter
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("=== ServerHeaderFilter initialized ===");
        log.info("Server information disclosure protection enabled");
    }

    /**
     * Remove or mask server-identifying headers from HTTP responses
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Wrap the response to intercept and modify headers
        ServerHeaderResponseWrapper wrappedResponse = new ServerHeaderResponseWrapper(httpResponse);

        // Continue with the filter chain
        chain.doFilter(request, wrappedResponse);

        // Remove server-identifying headers after the response is generated
        removeServerHeaders(wrappedResponse);

        // Log for audit purposes (only in debug mode)
        if (log.isDebugEnabled()) {
            log.debug("Removed server headers for: {} {}",
                httpRequest.getMethod(),
                httpRequest.getRequestURI());
        }
    }

    /**
     * Remove all server-related headers that could expose infrastructure details
     */
    private void removeServerHeaders(HttpServletResponse response) {
        // Remove common server headers
        response.setHeader("Server", ""); // Override any existing Server header

        // Remove X-Powered-By headers (PHP, ASP.NET, etc.)
        if (response.containsHeader("X-Powered-By")) {
            response.setHeader("X-Powered-By", "");
        }

        // Remove AWS ELB headers
        if (response.containsHeader("X-Amzn-Trace-Id")) {
            response.setHeader("X-Amzn-Trace-Id", "");
        }

        // Remove other infrastructure-revealing headers
        if (response.containsHeader("X-AspNet-Version")) {
            response.setHeader("X-AspNet-Version", "");
        }

        if (response.containsHeader("X-AspNetMvc-Version")) {
            response.setHeader("X-AspNetMvc-Version", "");
        }

        // Remove Servlet container info
        if (response.containsHeader("X-Application-Context")) {
            response.setHeader("X-Application-Context", "");
        }
    }

    /**
     * Clean up resources
     */
    @Override
    public void destroy() {
        log.info("=== ServerHeaderFilter destroyed ===");
    }

    /**
     * Response wrapper to intercept header setting
     */
    private static class ServerHeaderResponseWrapper extends javax.servlet.http.HttpServletResponseWrapper {

        public ServerHeaderResponseWrapper(HttpServletResponse response) {
            super(response);
        }

        /**
         * Intercept setHeader calls to prevent Server header from being set
         */
        @Override
        public void setHeader(String name, String value) {
            // Block Server and X-Powered-By headers
            if ("Server".equalsIgnoreCase(name) || "X-Powered-By".equalsIgnoreCase(name)) {
                // Don't set these headers
                return;
            }
            super.setHeader(name, value);
        }

        /**
         * Intercept addHeader calls to prevent Server header from being added
         */
        @Override
        public void addHeader(String name, String value) {
            // Block Server and X-Powered-By headers
            if ("Server".equalsIgnoreCase(name) || "X-Powered-By".equalsIgnoreCase(name)) {
                // Don't add these headers
                return;
            }
            super.addHeader(name, value);
        }
    }
}

