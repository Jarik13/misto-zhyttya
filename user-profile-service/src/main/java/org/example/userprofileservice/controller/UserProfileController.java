package org.example.userprofileservice.controller;

import lombok.RequiredArgsConstructor;
import org.example.userprofileservice.dto.profile.ProfileResponse;
import org.example.userprofileservice.grpc.UserProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/profiles")
@RequiredArgsConstructor
public class UserProfileController {
    private final UserProfileService userProfileService;

    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getMyProfile(@RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(userProfileService.findUserProfile(userId));
    }
}
