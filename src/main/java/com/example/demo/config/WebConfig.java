package com.example.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    // CORS is centrally and exclusively managed by Spring Security (SecurityConfig)
    // to avoid duplicate or conflicting Access-Control headers with withCredentials.
}
