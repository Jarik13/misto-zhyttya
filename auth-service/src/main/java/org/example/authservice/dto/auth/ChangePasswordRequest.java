package org.example.authservice.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @Schema(description = "Current password of the user", example = "OldPassword123!")
        @NotBlank(message = "Current password must not be empty")
        String currentPassword,

        @Schema(description = "New password with at least 6 characters, including uppercase, lowercase, digit, and special character",
                example = "NewStrongPassword1@")
        @NotBlank(message = "New password must not be empty")
        @Size(min = 6, message = "New password must be at least 6 characters")
        @Pattern(
                regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*\\W).*$",
                message = "New password must contain at least one uppercase letter, one lowercase letter, one digit and one special character")
        String newPassword,

        @Schema(description = "Confirmation of the new password", example = "NewStrongPassword1@")
        @NotBlank(message = "Confirm new password must not be empty")
        String confirmNewPassword
) {
}
