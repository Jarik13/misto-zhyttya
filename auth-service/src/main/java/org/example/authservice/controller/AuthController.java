package org.example.authservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.authservice.dto.MessageResponse;
import org.example.authservice.dto.auth.ChangePasswordRequest;
import org.example.authservice.dto.auth.LoginRequest;
import org.example.authservice.dto.auth.RegistrationRequest;
import org.example.authservice.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<MessageResponse> registerUser(@Valid @RequestBody RegistrationRequest request,
                                                        HttpServletResponse response) {
        authService.register(request, response);
        return ResponseEntity.ok(new MessageResponse("User registered successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<MessageResponse> loginUser(@Valid @RequestBody LoginRequest request,
                                                     HttpServletResponse response) {
        authService.login(request, response);
        return ResponseEntity.ok(new MessageResponse("User logged in successfully"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<Void> refreshAccessToken(HttpServletRequest request,
                                                   HttpServletResponse response) {
        authService.refreshAccessToken(request, response);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logoutUser(HttpServletRequest request,
                                                      HttpServletResponse response) {
        authService.logout(request, response);
        return ResponseEntity.ok(new MessageResponse("User logged out successfully"));
    }

    @PostMapping("/change-password")
    public ResponseEntity<MessageResponse> changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                                          HttpServletRequest httpRequest) {
        authService.changePassword(httpRequest, request);
        return ResponseEntity.ok(new MessageResponse("Password changed successfully"));
    }
}
