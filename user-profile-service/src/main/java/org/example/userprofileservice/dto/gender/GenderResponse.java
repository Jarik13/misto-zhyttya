package org.example.userprofileservice.dto.gender;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Gender representation with numeric value and human-readable label")
public record GenderResponse(
        @Schema(description = "Numeric value representing gender enum ordinal", example = "0")
        Long value,

        @Schema(description = "Human-readable gender label", example = "Male")
        String label
) {
}
