package com.dechub.tanishq.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final Logger log = LoggerFactory.getLogger(WebConfig.class);

    @Autowired
    private Environment environment;

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Maps "/about" to "/about.html"
        registry.addViewController("/events").setViewName("forward:/events.html");
        registry.addViewController("/contact").setViewName("forward:/contact.html");
        registry.addViewController("/home").setViewName("forward:/home.html");
        // Note: /events/customer/{eventId} is handled by EventsController, not here
        // The greeting card QR feature uses a different route
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve locally stored files in local profile
        if (Arrays.asList(environment.getActiveProfiles()).contains("local")) {
            registry.addResourceHandler("/storage/**")
                    .addResourceLocations("file:./storage/");
            log.info("Enabled local file serving at /storage/**");
        }
    }
}
