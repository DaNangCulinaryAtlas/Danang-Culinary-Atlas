package com.atlasculinary.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configuration class for Cross-Origin Resource Sharing (CORS).
 * CORS is a security feature implemented by browsers that restricts web pages from making requests
 * to a different domain than the one that served the original page.
 *
 * This configuration allows specific origins (domains) to access our API endpoints.
 */
@Configuration
public class CorsConfig {

    @Value("${app.url.allowed.cors}")
    private String urlAllowedCors;

    /**
     * Creates and configures a CORS filter bean.
     * This filter will be applied to all incoming requests to handle CORS headers.
     *
     * The configuration includes:
     * - Allowed origins: Specifies which domains can access our API
     * - Allowed methods: HTTP methods that are permitted (GET, POST, etc.)
     * - Allowed headers: Headers that can be used in the actual request
     * - Exposed headers: Headers that browsers are allowed to access
     * - Credentials: Whether to allow cookies and authentication headers
     *
     * @return CorsFilter bean with configured CORS settings
     */
    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // 👈 Frontend domain - loaded from environment variable FRONTEND_URL
        // config.setAllowedOrigins(List.of(urlAllowedCors));
        config.setAllowedOriginPatterns(List.of("*"));
        // 👈 Allow all origins for development purposes
        // 👈 Allowed HTTP methods
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH","DELETE", "OPTIONS"));

        // 👈 Headers that can be used in the actual request
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "X-Requested-With", "Origin", "Access-Control-Allow-Origin"));

        // 👈 Headers that browsers are allowed to access in the response
        config.setExposedHeaders(List.of("Authorization"));

        // 👈 Allow cookies and authentication headers to be sent with requests
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 👈 Apply this CORS configuration to all paths except WebSocket
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}