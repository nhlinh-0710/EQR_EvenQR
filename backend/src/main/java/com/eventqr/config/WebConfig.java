package com.eventqr.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        // Serve ảnh từ thư mục /uploads/
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:uploads/"); // trỏ tới backend/uploads
    }
}
