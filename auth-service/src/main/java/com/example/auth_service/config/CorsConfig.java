package com.example.auth_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // Mengizinkan kredensial (misal cookie, token, dll) dikirimkan dalam request
        config.setAllowCredentials(true);

        // Mengizinkan origin tertentu (bisa juga menggunakan * untuk semua origin)
        config.addAllowedOrigin("http://localhost:3000"); // atau http://frontend-domain.com

        // Mengizinkan method HTTP tertentu
        config.addAllowedMethod("*"); // Izinkan semua method (GET, POST, PUT, DELETE, dll)

        // Mengizinkan header tertentu
        config.addAllowedHeader("*"); // Izinkan semua header

        // Menentukan lama cache CORS preflight request (OPTIONS)
        config.setMaxAge(3600L); // Preflight cache 1 jam

        source.registerCorsConfiguration("/**", config); // Berlaku untuk semua endpoint
        return new CorsFilter(source);
    }
}
