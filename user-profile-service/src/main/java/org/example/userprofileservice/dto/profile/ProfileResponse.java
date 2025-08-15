package org.example.userprofileservice.dto.profile;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record ProfileResponse(
        @Schema(description = "Ім'я користувача", example = "john_doe")
        String username,

        @Schema(description = "Номер телефону у міжнародному форматі", example = "+380991234567")
        String phoneNumber,

        @Schema(description = "Дата народження у форматі yyyy-MM-dd", example = "2000-01-01")
        LocalDate dateOfBirth,

        @Schema(description = "Ідентифікатор статі (0 - чоловік, 1 - жінка, 2 - трансгендер, 3 - не вказано)", example = "0")
        Long genderId,

        @Schema(description = "Ключ аватарки користувача", example = "123e4567-e89b-12d3-a456-426614174000-avatar.png")
        String avatarKey
) {
}
