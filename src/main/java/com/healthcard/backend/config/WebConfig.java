package com.healthcard.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.file-storage.upload-dir}")
    private String uploadDir;

    /** Serves uploaded lab report attachments at /uploads/lab-reports/** */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/lab-reports/**")
                .addResourceLocations("file:" + uploadDir + "/");
    }
}
