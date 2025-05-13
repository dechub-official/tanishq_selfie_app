package com.dechub.tanishq.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Maps "/about" to "/about.html"
        registry.addViewController("/events").setViewName("forward:/events.html");
        registry.addViewController("/contact").setViewName("forward:/contact.html");
        registry.addViewController("/home").setViewName("forward:/home.html");
    }
}
