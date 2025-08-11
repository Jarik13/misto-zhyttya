package org.example.userprofileservice.dto.profile;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import org.example.userprofileservice.annotation.UniquePhoneNumber;

import java.time.LocalDate;

public record ProfileRequest(
        @Schema(description = "Ім'я користувача", example = "john_doe")
        @NotBlank(message = "Ім'я користувача не має бути порожнім")
        @Size(min = 3, max = 30, message = "Ім'я користувача має бути від 3 до 30 символів")
        String username,

        @Schema(description = "Номер телефону у міжнародному форматі", example = "+380991234567")
        @NotBlank(message = "Номер телефону не має бути порожнім")
        @UniquePhoneNumber
        @Pattern(regexp = "^\\+?[0-9]{10,13}$", message = "Номер телефону має містити від 10 до 13 цифр та може починатися з +")
        String phoneNumber,

        @Schema(description = "Дата народження у форматі yyyy-MM-dd", example = "2000-01-01")
        @NotNull(message = "Дата народження не може бути порожньою")
        @Past(message = "Дата народження має бути у минулому")
        LocalDate dateOfBirth,

        @Schema(description = "Ідентифікатор статі (0 - чоловік, 1 - жінка, 2 - трансгендер, 3 - не вказано)", example = "0")
        @NotNull(message = "Ідентифікатор статі не може бути порожнім")
        @Min(value = 0, message = "Ідентифікатор статі має бути не менше 0")
        @Max(value = 3, message = "Ідентифікатор статі має бути не більше 3")
        Long genderId,

        @Schema(description = "Ключ аватарки користувача", example = "123e4567-e89b-12d3-a456-426614174000-avatar.png")
        String avatarKey
) {
}
