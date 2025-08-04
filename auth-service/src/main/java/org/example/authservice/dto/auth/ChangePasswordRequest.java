package org.example.authservice.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank(message = "Current password must not be empty")
        String currentPassword,

        @NotBlank(message = "New password must not be empty")
        @Size(min = 6, message = "New password must be at least 6 characters")
        @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*\\W).*$",
                message = "New password must contain at least one uppercase letter, one lowercase letter, one digit and one special character")
        String newPassword,

        @NotBlank(message = "Confirm new password must not be empty")
        String confirmNewPassword
) {
}
