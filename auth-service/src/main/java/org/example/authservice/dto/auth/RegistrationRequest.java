package org.example.authservice.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record RegistrationRequest(
        @Schema(description = "Ім'я користувача", example = "john_doe")
        @NotBlank(message = "Ім'я користувача не може бути порожнім")
        @Size(min = 3, max = 30, message = "Ім'я користувача має бути від 3 до 30 символів")
        String username,

        @Schema(description = "Електронна пошта користувача", example = "john.doe@example.com")
        @NotBlank(message = "Електронна пошта не може бути порожньою")
        @Email(message = "Електронна пошта повинна бути валідною")
        String email,

        @Schema(description = "Пароль користувача. Має містити принаймні одну велику літеру, одну малу літеру, одну цифру та один спеціальний символ", example = "Password123!")
        @NotBlank(message = "Пароль не може бути порожнім")
        @Size(min = 6, message = "Пароль повинен містити щонайменше 6 символів")
        @Pattern(
                regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*\\W).*$",
                message = "Пароль повинен містити принаймні одну велику літеру, одну малу літеру, одну цифру та один спеціальний символ"
        )
        String password,

        @Schema(description = "Підтвердження пароля", example = "Password123!")
        @NotBlank(message = "Підтвердження пароля не може бути порожнім")
        String confirmPassword,

        @Schema(description = "Номер телефону у міжнародному форматі", example = "+380991234567")
        @NotBlank(message = "Номер телефону не може бути порожнім")
        @Pattern(regexp = "^\\+?[0-9]{10,13}$", message = "Номер телефону має містити від 10 до 13 цифр та може починатися з +")
        String phoneNumber,

        @Schema(description = "Дата народження у форматі yyyy-MM-dd", example = "2000-01-01")
        @NotNull(message = "Дата народження не може бути порожньою")
        @Past(message = "Дата народження має бути в минулому")
        LocalDate dateOfBirth,

        @Schema(
                description = "Стать користувача (0 - чоловік, 1 - жінка, 2 - трансгендер, 3 - не вказано)",
                example = "0"
        )
        @NotNull(message = "Стать не може бути порожньою")
        @Min(value = 0, message = "Стать повинна мати значення не менше за 0")
        @Max(value = 3, message = "Стать повинна мати значення не більше за 3")
        Long genderId
) {
}
