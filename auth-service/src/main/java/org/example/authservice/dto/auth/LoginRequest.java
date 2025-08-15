package org.example.authservice.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @Schema(description = "Електронна пошта користувача", example = "user@example.com")
        @NotBlank(message = "Електронна пошта не повинна бути порожньою")
        @Email(message = "Електронна пошта повинна бути валідною")
        String email,

        @Schema(description = "Пароль користувача", example = "Password123!")
        @NotBlank(message = "Пароль не може бути порожнім")
        @Size(min = 6, message = "Пароль повинен містити щонайменше 6 символів")
        String password
) {
}
