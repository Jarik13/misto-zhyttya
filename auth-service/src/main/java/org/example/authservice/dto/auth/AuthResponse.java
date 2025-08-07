package org.example.authservice.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response object for authentication requests")
public record AuthResponse(
        @Schema(description = "Response message", example = "User registered successfully")
        String message,

        @JsonProperty("access_token")
        @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String accessToken
) {
}
