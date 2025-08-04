package org.example.authservice.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @Schema(description = "User email", example = "user@example.com")
        @NotBlank(message = "Email must not be empty")
        @Email(message = "Email should be valid")
        String email,

        @Schema(description = "User password", example = "Password123!")
        @NotBlank(message = "Password must not be empty")
        @Size(min = 6, message = "Password must be at least 6 characters")
        String password
) {
}