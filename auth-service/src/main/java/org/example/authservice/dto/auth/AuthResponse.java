package org.example.authservice.dto.auth;

public record AuthResponse(
        String message,
        String accessToken
) {
}
