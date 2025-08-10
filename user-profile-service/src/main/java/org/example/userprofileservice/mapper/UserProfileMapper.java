package org.example.userprofileservice.mapper;

import org.example.userprofileservice.dto.profile.ProfileResponse;
import org.example.userprofileservice.model.Gender;
import user.profile.CreateUserProfileRequest;
import org.example.userprofileservice.model.Profile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class UserProfileMapper {
    public static Profile toUserProfile(CreateUserProfileRequest request) {
        LocalDate dateOfBirth = null;
        String dobString = request.getDateOfBirth();

        if (!dobString.isEmpty()) {
            dateOfBirth = LocalDate.parse(dobString, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }

        return Profile.builder()
                .username(request.getUsername())
                .phoneNumber(request.getPhoneNumber())
                .dateOfBirth(dateOfBirth)
                .gender(Gender.fromId(request.getGenderId()))
                .avatarUrl(request.getAvatarUrl())
                .userId(request.getUserId())
                .build();
    }

    public static ProfileResponse toUserProfileResponse(Profile profile) {
        return ProfileResponse.builder()
                .username(profile.getUsername())
                .phoneNumber(profile.getPhoneNumber())
                .dateOfBirth(profile.getDateOfBirth())
                .genderId(profile.getGender().getId())
                .avatarUrl(profile.getAvatarUrl())
                .build();
    }
}
