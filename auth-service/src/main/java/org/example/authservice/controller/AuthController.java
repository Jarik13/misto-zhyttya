package org.example.authservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.authservice.dto.MessageResponse;
import org.example.authservice.dto.auth.ChangePasswordRequest;
import org.example.authservice.dto.auth.LoginRequest;
import org.example.authservice.dto.auth.RegistrationRequest;
import org.example.authservice.dto.auth.ValidationResponse;
import org.example.authservice.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user authentication and authorization")
public class AuthController {
    private final AuthService authService;

    @Operation(summary = "Register a new user")
    @PostMapping("/register")
    public ResponseEntity<MessageResponse> registerUser(@Valid @RequestBody RegistrationRequest request,
                                                        HttpServletResponse response) {
        authService.register(request, response);
        return ResponseEntity.ok(new MessageResponse("User registered successfully"));
    }

    @Operation(summary = "Log in a user")
    @PostMapping("/login")
    public ResponseEntity<MessageResponse> loginUser(@Valid @RequestBody LoginRequest request,
                                                     HttpServletResponse response) {
        authService.login(request, response);
        return ResponseEntity.ok(new MessageResponse("User logged in successfully"));
    }

    @Operation(summary = "Refresh access token using refresh token")
    @PostMapping("/refresh")
    public ResponseEntity<Void> refreshAccessToken(HttpServletRequest request,
                                                   HttpServletResponse response) {
        authService.refreshAccessToken(request, response);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Log out a user and invalidate tokens")
    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logoutUser(HttpServletRequest request,
                                                      HttpServletResponse response) {
        authService.logout(request, response);
        return ResponseEntity.ok(new MessageResponse("User logged out successfully"));
    }

    @Operation(summary = "Change user password")
    @PostMapping("/change-password")
    public ResponseEntity<MessageResponse> changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                                          HttpServletRequest httpRequest) {
        authService.changePassword(httpRequest, request);
        return ResponseEntity.ok(new MessageResponse("Password changed successfully"));
    }

    @Operation(summary = "Validate access token")
    @GetMapping("/validate")
    public ResponseEntity<ValidationResponse> validateToken(@CookieValue(value = "access_token", required = false) String token) {
        return token == null ?
                ResponseEntity.status(HttpStatus.UNAUTHORIZED).build() :
                ResponseEntity.ok(new ValidationResponse(authService.validateToken(token)));
    }
}
