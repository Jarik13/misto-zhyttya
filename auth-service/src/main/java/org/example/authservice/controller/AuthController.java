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
@Tag(name = "Аутентифікація", description = "Ендпоїнти для аутентифікації та авторизації користувачів")
public class AuthController {
    private final AuthService authService;

    @Operation(summary = "Реєстрація нового користувача")
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registerUser(@Valid @RequestBody RegistrationRequest request,
                                                     HttpServletResponse response) {
        return ResponseEntity.ok(authService.register(request, response));
    }

    @Operation(summary = "Вхід користувача в систему")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginUser(@Valid @RequestBody LoginRequest request,
                                                  HttpServletResponse response) {
        return ResponseEntity.ok(authService.login(request, response));
    }

    @Operation(summary = "Оновлення access токена за допомогою refresh токена")
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshAccessToken(HttpServletRequest request,
                                                           HttpServletResponse response) {
        return ResponseEntity.ok(authService.refreshAccessToken(request, response));
    }

    @Operation(summary = "Вихід користувача з системи та ануляція токенів")
    @PostMapping("/logout")
    public ResponseEntity<Void> logoutUser(HttpServletRequest request,
                                           HttpServletResponse response) {
        authService.logout(request, response);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Зміна пароля користувача")
    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                               HttpServletRequest httpRequest) {
        authService.changePassword(httpRequest, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Перевірка валідності access токена")
    @GetMapping("/validate")
    public ResponseEntity<String> validateToken(@RequestHeader(value = "Authorization", required = false) String token) {
        return token == null ?
                ResponseEntity.status(HttpStatus.UNAUTHORIZED).build() :
                ResponseEntity.ok(authService.validateToken(token));
    }
}
