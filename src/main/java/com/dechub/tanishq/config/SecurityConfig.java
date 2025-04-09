package com.dechub.tanishq.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http
                .authorizeRequests()
                .antMatchers(HttpMethod.TRACE, "/**").denyAll()
                .antMatchers("/checklist/**").permitAll()
                .anyRequest().permitAll()
                .and().httpBasic()
                .and().sessionManagement().sessionFixation().changeSessionId()
                .and().csrf().disable()
                .headers().frameOptions().disable()
                .addHeaderWriter(new StaticHeadersWriter("Access-Control-Allow-Methods","GET", "POST"))
                .addHeaderWriter(new StaticHeadersWriter("Access-Control-Max-Age", "3600"))
                .addHeaderWriter(new StaticHeadersWriter("Access-Control-Allow-Credentials", "true"))
                .addHeaderWriter(new StaticHeadersWriter("Access-Control-Allow-Headers", "X-CSRF-TOKEN,X-Frame-Options"))
                .addHeaderWriter(new StaticHeadersWriter("X-Frame-Options", "DENY"))
                .xssProtection()
                .and()
//                .referrerPolicy().and().permissionsPolicy().policy("camera=*, fullscreen=(self), geolocation=(), gyroscope=(), magnetometer=(), microphone=(), midi=(), payment=(), sync-xhr=()").and()
                .contentSecurityPolicy("form-action 'self'");
                http.headers()
                .addHeaderWriter(new StaticHeadersWriter("Server","Apache Tomcat"));

                http.headers().httpStrictTransportSecurity().requestMatcher(AnyRequestMatcher.INSTANCE);

    }



}