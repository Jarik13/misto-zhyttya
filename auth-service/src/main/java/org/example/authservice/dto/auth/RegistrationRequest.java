package org.example.authservice.dto.auth;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record RegistrationRequest(
        @NotBlank(message = "First name must not be empty")
        String firstName,

        @NotBlank(message = "Last name must not be empty")
        String lastName,

        @NotBlank(message = "Email must not be empty")
        @Email(message = "Email should be valid")
        String email,

        @NotBlank(message = "Password must not be empty")
        @Size(min = 6, message = "Password must be at least 6 characters")
        @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*\\W).*$",
                message = "Password must contain at least one uppercase letter, one lowercase letter, one digit and one special character")
        String password,

        @NotBlank(message = "Confirm password must not be empty")
        String confirmPassword,

        @NotBlank(message = "Phone number must not be empty")
        @Pattern(regexp = "^\\+?[0-9]{10,13}$", message = "Phone number must be between 10 and 13 digits and may start with +")
        String phoneNumber,

        @NotNull(message = "Date of birth must not be null")
        @Past(message = "Date of birth must be in the past")
        LocalDate dateOfBirth,

        @NotBlank(message = "Gender must not be empty")
        @Pattern(regexp = "^(?i)(male|female|other)$", message = "Gender must be 'male', 'female' or 'other'")
        String gender,

        @Pattern(regexp = "^(https?://).+", message = "Avatar URL must be a valid URL")
        String avatarUrl
) {
}
