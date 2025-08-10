package org.example.userprofileservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.userprofileservice.dto.profile.ProfileResponse;
import org.example.userprofileservice.grpc.UserProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User Profiles", description = "Endpoints for retrieving and managing user profiles")
@RestController
@RequestMapping("/api/v1/profiles")
@RequiredArgsConstructor
public class UserProfileController {
    private final UserProfileService userProfileService;

    @Operation(
            summary = "Get the current user's profile",
            description = "Returns detailed profile information for the currently authenticated user."
    )
    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getMyProfile(
            @Parameter(description = "Unique identifier of the authenticated user", example = "550e8400-e29b-41d4-a716-446655440000")
            @RequestHeader("X-User-Id") String userId
    ) {
        return ResponseEntity.ok(userProfileService.findUserProfile(userId));
    }
}
