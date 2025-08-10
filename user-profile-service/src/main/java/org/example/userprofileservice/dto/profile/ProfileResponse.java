package org.example.userprofileservice.dto.profile;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record ProfileResponse(
        @Schema(description = "Username", example = "john_doe")
        String username,

        @Schema(description = "Phone number in international format", example = "+380991234567")
        String phoneNumber,

        @Schema(description = "Date of birth in format yyyy-MM-dd", example = "2000-01-01")
        LocalDate dateOfBirth,

        @Schema(description = "Gender ID (0 - male, 1 - female, 2 - transgender, 3 - not specified)", example = "0")
        Long genderId,

        @Schema(description = "URL to the user's avatar", example = "https://example.com/avatar.jpg")
        String avatarUrl
) {
}
