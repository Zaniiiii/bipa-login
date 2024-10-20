//package com.example.gateway_service.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
//import org.springframework.security.config.web.server.ServerHttpSecurity;
//import org.springframework.security.web.server.SecurityWebFilterChain;
//
//@Configuration
//@EnableWebFluxSecurity
//public class SecurityConfig {
//
//    @Bean
//    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
//        http
//                .csrf(csrf -> csrf.disable()) // Nonaktifkan CSRF
//                .authorizeExchange(exchange -> exchange
//                        .pathMatchers("/auth/**").permitAll() // Izinkan akses ke /auth tanpa autentikasi
//                        .anyExchange().authenticated() // Semua rute lainnya harus diautentikasi
//                )
//                .oauth2ResourceServer(ServerHttpSecurity.OAuth2ResourceServerSpec::jwt); // Gunakan JWT untuk otorisasi
//        return http.build();
//    }
//}
