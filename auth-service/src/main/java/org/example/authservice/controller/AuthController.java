package org.example.authservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.authservice.dto.auth.*;
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
    public ResponseEntity<AuthResponse> registerUser(@Valid @RequestBody RegistrationRequest request,
                                                     HttpServletResponse response) {
        return ResponseEntity.ok(authService.register(request, response));
    }

    @Operation(summary = "Log in a user")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginUser(@Valid @RequestBody LoginRequest request,
                                                     HttpServletResponse response) {
        return ResponseEntity.ok(authService.login(request, response));
    }

    @Operation(summary = "Refresh access token using refresh token")
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshAccessToken(HttpServletRequest request,
                                                   HttpServletResponse response) {
        return ResponseEntity.ok(authService.refreshAccessToken(request, response));
    }

    @Operation(summary = "Log out a user and invalidate tokens")
    @PostMapping("/logout")
    public ResponseEntity<Void> logoutUser(HttpServletRequest request,
                                                      HttpServletResponse response) {
        authService.logout(request, response);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Change user password")
    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                                          HttpServletRequest httpRequest) {
        authService.changePassword(httpRequest, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Validate access token")
    @GetMapping("/validate")
    public ResponseEntity<String> validateToken(@RequestHeader(value = "Authorization", required = false) String token) {
        return token == null ?
                ResponseEntity.status(HttpStatus.UNAUTHORIZED).build() :
                ResponseEntity.ok(authService.validateToken(token));
    }
}
