package org.example.authservice.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Об'єкт відповіді на запити аутентифікації")
public record AuthResponse(
        @JsonProperty("access_token")
        @Schema(description = "JWT токен доступу", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String accessToken
) {
}
