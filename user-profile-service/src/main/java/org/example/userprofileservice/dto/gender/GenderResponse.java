package org.example.userprofileservice.dto.gender;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Подання статі з числовим значенням та зрозумілою назвою")
public record GenderResponse(
        @Schema(description = "Числове значення, що відповідає порядковому номеру статі", example = "0")
        Long value,

        @Schema(description = "Зрозуміла людиною назва статі", example = "Чоловік")
        String label
) {
}
