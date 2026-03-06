package com.dechub.tanishq.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http
                .authorizeRequests()
                // SECURITY FIX: HTTP Methods Restriction (OWASP A05)
                // Block unnecessary HTTP methods to reduce attack surface
                .antMatchers(HttpMethod.TRACE, "/**").denyAll()      // Block TRACE method
                .antMatchers(HttpMethod.PUT, "/**").denyAll()        // Block PUT method
                .antMatchers(HttpMethod.DELETE, "/**").denyAll()     // Block DELETE method
                .antMatchers(HttpMethod.PATCH, "/**").denyAll()      // Block PATCH method
                // Allow only necessary HTTP methods (GET, POST, OPTIONS)
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()  // Allow OPTIONS for CORS preflight
                .antMatchers("/checklist/**").permitAll()
                .antMatchers("/greetings/**").permitAll()  // Explicitly allow greetings endpoints
                .antMatchers("/events/customer/**").permitAll()  // Allow public QR code access
                .anyRequest().permitAll()
                .and().httpBasic()
                // SECURITY FIX: Enable proper session management with fixation protection
                .and().sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                    .sessionFixation().changeSessionId()  // Mitigate session fixation attacks
                    .maximumSessions(1)  // Limit to one session per user
                    .maxSessionsPreventsLogin(false)  // Allow new login to invalidate old session
                .and()
                .and().csrf().disable()  // Disabled for REST API (would require token management)
                .cors()  // Enable CORS support
                .and()
                .headers().frameOptions().disable()
                .addHeaderWriter(new StaticHeadersWriter("Access-Control-Allow-Methods","GET, POST, OPTIONS"))
                .addHeaderWriter(new StaticHeadersWriter("Access-Control-Max-Age", "3600"))
                .addHeaderWriter(new StaticHeadersWriter("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With, Accept, Origin"))
                .addHeaderWriter(new StaticHeadersWriter("X-Frame-Options", "DENY"))
                .addHeaderWriter(new StaticHeadersWriter("X-Content-Type-Options", "nosniff"))  // Prevent MIME sniffing
                .xssProtection()
                .and()
//                .referrerPolicy().and().permissionsPolicy().policy("camera=*, fullscreen=(self), geolocation=(), gyroscope=(), magnetometer=(), microphone=(), midi=(), payment=(), sync-xhr=()").and()
                .contentSecurityPolicy("form-action 'self'");

                // SECURITY FIX: Server information disclosure prevention (OWASP A05)
                // Server header is now handled by ServerHeaderFilter to completely suppress it
                // Removed: .addHeaderWriter(new StaticHeadersWriter("Server","Apache Tomcat"));

                http.headers().httpStrictTransportSecurity().requestMatcher(AnyRequestMatcher.INSTANCE);

    }



}