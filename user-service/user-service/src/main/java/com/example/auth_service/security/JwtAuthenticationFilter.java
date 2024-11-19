package com.example.auth_service.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final JwtTokenProvider jwtTokenProvider;

    private static final List<String> EXCLUDED_URLS = Arrays.asList(
            "/api/auth/register",
            "/api/auth/verify",
            "/api/auth/login",
            "/api/auth/forgot-password",
            "/api/auth/reset-password",
            "/api/auth/validate-reset-token"
    );


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        logger.info("Request URI: {}", request.getRequestURI());

        String requestURI = request.getRequestURI();

        // Skip JWT processing for excluded URLs
        if (EXCLUDED_URLS.contains(requestURI)) {
            logger.info("Skipping JWT Filter for endpoint: {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            logger.info("Authorization header missing or does not start with Bearer");
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);
        if (jwtTokenProvider.validateToken(token)) {
            String email = jwtTokenProvider.getEmail(token);
            String role = jwtTokenProvider.getRole(token);  // Ambil role dari token
            String userName = jwtTokenProvider.getUserName(token);
            String userUuid = jwtTokenProvider.getUserUuid(token);

            // Set GrantedAuthority berdasarkan role dari JWT
            List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));

            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(email, null, authorities);
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(auth);

            logger.info("JWT Token validated successfully for user: {}, with role: {}", email, role);
        } else {
            logger.info("Invalid JWT token");
        }

        logger.info("Proceeding with the request after JWT check.");
        filterChain.doFilter(request, response);
    }
}
