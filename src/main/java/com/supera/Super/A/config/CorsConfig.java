package com.supera.Super.A.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("Content-Type", "Authorization", "Accept")
                .exposedHeaders("Access-Control-Allow-Origin", "Access-Control-Allow-Methods", "Access-Control-Allow-Headers")
                .allowCredentials(false)
                .maxAge(3600);
    }
}
