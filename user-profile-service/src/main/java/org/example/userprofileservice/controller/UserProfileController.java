package org.example.userprofileservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.userprofileservice.dto.profile.ProfileRequest;
import org.example.userprofileservice.dto.profile.ProfileResponse;
import org.example.userprofileservice.grpc.UserProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Профілі користувачів", description = "Ендпоїнти для отримання та керування профілями користувачів")
@RestController
@RequestMapping("/api/v1/profiles")
@RequiredArgsConstructor
public class UserProfileController {
    private final UserProfileService userProfileService;

    @Operation(
            summary = "Отримати профіль поточного користувача",
            description = "Повертає детальну інформацію профілю для автентифікованого користувача."
    )
    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getMyProfile(
            @Parameter(description = "Унікальний ідентифікатор автентифікованого користувача", example = "550e8400-e29b-41d4-a716-446655440000")
            @RequestHeader("X-User-Id") String userId
    ) {
        return ResponseEntity.ok(userProfileService.findUserProfile(userId));
    }

    @Operation(
            summary = "Оновити профіль поточного користувача",
            description = "Виконує повне оновлення профілю користувача. Всі поля обов'язкові."
    )
    @PutMapping
    public ResponseEntity<Void> updateUserProfile(
            @Parameter(description = "Унікальний ідентифікатор автентифікованого користувача", example = "550e8400-e29b-41d4-a716-446655440000")
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody ProfileRequest request
    ) {
        userProfileService.updateUserProfile(userId, request);
        return ResponseEntity.noContent().build();
    }
}
