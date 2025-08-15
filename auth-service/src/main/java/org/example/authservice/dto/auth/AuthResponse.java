package org.example.authservice.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Об'єкт відповіді на запити аутентифікації")
public record AuthResponse(
        @JsonProperty("access_token")
        @Schema(description = "JWT токен доступу", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String accessToken,

        @Schema(description = "Роль користувача", example = "USER")
        String role,

        @Schema(description = "Профіль користувача")
        Profile profile
) {
        @Schema(description = "Дані профілю користувача")
        public record Profile(
                @Schema(description = "Ім'я користувача", example = "john_doe")
                String username,

                @Schema(description = "Інформація про аватарку користувача")
                String avatarKey
        ) {}
}
