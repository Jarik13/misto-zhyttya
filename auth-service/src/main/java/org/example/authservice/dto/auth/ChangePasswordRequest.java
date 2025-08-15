package org.example.authservice.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @Schema(description = "Поточний пароль користувача", example = "OldPassword123!")
        @NotBlank(message = "Поточний пароль не може бути порожнім")
        String currentPassword,

        @Schema(description = "Новий пароль, мінімум 6 символів, що містить велику і малу літери, цифру та спеціальний символ",
                example = "NewStrongPassword1@")
        @NotBlank(message = "Новий пароль не може бути порожнім")
        @Size(min = 6, message = "Новий пароль повинен містити щонайменше 6 символів")
        @Pattern(
                regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*\\W).*$",
                message = "Новий пароль повинен містити щонайменше одну велику літеру, одну малу літеру, одну цифру та один спеціальний символ")
        String newPassword,

        @Schema(description = "Підтвердження нового пароля", example = "NewStrongPassword1@")
        @NotBlank(message = "Підтвердження нового пароля не може бути порожнім")
        String confirmNewPassword
) {
}
