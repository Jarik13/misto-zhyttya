package org.example.authservice.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record RegistrationRequest(
        @Schema(description = "Username", example = "john_doe")
        @NotBlank(message = "Username must not be empty")
        String username,

        @Schema(description = "User's email", example = "john.doe@example.com")
        @NotBlank(message = "Email must not be empty")
        @Email(message = "Email should be valid")
        String email,

        @Schema(description = "User's password. Must contain at least one uppercase letter, one lowercase letter, one digit and one special character", example = "Password123!")
        @NotBlank(message = "Password must not be empty")
        @Size(min = 6, message = "Password must be at least 6 characters")
        @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*\\W).*$",
                message = "Password must contain at least one uppercase letter, one lowercase letter, one digit and one special character")
        String password,

        @Schema(description = "Password confirmation", example = "Password123!")
        @NotBlank(message = "Confirm password must not be empty")
        String confirmPassword,

        @Schema(description = "Phone number in international format", example = "+380991234567")
        @NotBlank(message = "Phone number must not be empty")
        @Pattern(regexp = "^\\+?[0-9]{10,13}$", message = "Phone number must be between 10 and 13 digits and may start with +")
        String phoneNumber,

        @Schema(description = "Date of birth in format yyyy-MM-dd", example = "2000-01-01")
        @NotNull(message = "Date of birth must not be null")
        @Past(message = "Date of birth must be in the past")
        LocalDate dateOfBirth,

        @Schema(description = "Gender ID (0 - male, 1 - female, 2 - transgender, 3 - not specified)", example = "0")
        @NotNull(message = "Gender ID must not be null")
        @Min(value = 0, message = "Gender ID must be at least 0")
        @Max(value = 3, message = "Gender ID must be at most 3")
        Long genderId,

        @Schema(description = "URL to the user's avatar", example = "https://example.com/avatar.jpg")
        @Pattern(regexp = "^(https?://).+", message = "Avatar URL must be a valid URL")
        String avatarUrl
) {
}
