package org.example.userprofileservice.dto.profile;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record ProfileRequest(
        @NotBlank(message = "Username must not be empty")
        String username,

        @NotBlank(message = "Phone number must not be empty")
        @Pattern(regexp = "^\\+?[0-9]{10,13}$", message = "Phone number must be between 10 and 13 digits and may start with +")
        String phoneNumber,

        @NotNull(message = "Date of birth must not be null")
        @Past(message = "Date of birth must be in the past")
        LocalDate dateOfBirth,

        @NotNull(message = "Gender ID must not be null")
        @Min(value = 0, message = "Gender ID must be at least 0")
        @Max(value = 3, message = "Gender ID must be at most 3")
        Long genderId,

        @Pattern(regexp = "^(https?://).+", message = "Avatar URL must be a valid URL")
        String avatarUrl
) {
}
