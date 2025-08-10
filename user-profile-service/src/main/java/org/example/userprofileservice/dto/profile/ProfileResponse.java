package org.example.userprofileservice.dto.profile;

import java.time.LocalDate;

public record ProfileResponse(
        String username,
        String phoneNumber,
        LocalDate dateOfBirth,
        Long genderId,
        String avatarUrl
) {
}
