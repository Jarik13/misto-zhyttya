package org.example.apigateway.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.cors.CorsConfiguration;

import static org.junit.jupiter.api.Assertions.*;

public class CorsConfigTests {
    @Test
    void givenCorsConfig_whenCreated_thenConfigurationIsCorrect() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOriginPattern("http://localhost:*");
        config.addAllowedOriginPattern("https://localhost:*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");

        assertTrue(config.getAllowCredentials(), "AllowCredentials should be true");
        assertTrue(config.getAllowedOriginPatterns().contains("http://localhost:*"));
        assertTrue(config.getAllowedOriginPatterns().contains("https://localhost:*"));
        assertTrue(config.getAllowedHeaders().contains("*"));
        assertTrue(config.getAllowedMethods().contains("*"));
    }
}
