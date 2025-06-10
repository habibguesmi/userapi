package com.example.userapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {

            @Override
            public void addCorsMappings(CorsRegistry registry) {
                String[] allowedOrigins = new String[]{
                        "https://habibguesmi.github.io",
                        "http://localhost:4200",
                        "http://localhost:5173",
                        "https://user-app-angular.onrender.com"
                };

                // CORS pour API REST (ex: /api/**)
                registry.addMapping("/api/**")
                        .allowedOrigins(allowedOrigins)
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);

                // CORS pour WebSocket SockJS fallback (ex: /ws/**)
                registry.addMapping("/ws/**")
                        .allowedOrigins(allowedOrigins)
                        .allowedMethods("GET", "POST", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }
}
