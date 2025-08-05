package org.example.authservice.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.example.authservice.dto.auth.ChangePasswordRequest;
import org.example.authservice.dto.auth.LoginRequest;
import org.example.authservice.dto.auth.RegistrationRequest;
import org.example.authservice.exception.BusinessException;
import org.example.authservice.mapper.UserMapper;
import org.example.authservice.model.User;
import org.example.authservice.repository.UserRepository;
import org.example.authservice.security.CookieUtils;
import org.example.authservice.security.JwtService;
import org.example.authservice.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceImplTests {
    private UserMapper userMapper;
    private JwtService jwtService;
    private CookieUtils cookieUtils;
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private AuthenticationManager authenticationManager;

    private AuthServiceImpl authService;
    private Validator validator;

    @BeforeEach
    void setUp() {
        userMapper = mock(UserMapper.class);
        jwtService = mock(JwtService.class);
        cookieUtils = mock(CookieUtils.class);
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        authenticationManager = mock(AuthenticationManager.class);

        authService = new AuthServiceImpl(
                userMapper,
                jwtService,
                cookieUtils,
                userRepository,
                passwordEncoder,
                authenticationManager
        );

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // region: Registration Tests

    @Test
    void givenValidRegistrationRequest_whenRegister_thenSaveUserAndAddCookies() {
        RegistrationRequest request = new RegistrationRequest(
                "John",
                "Doe",
                "john@example.com",
                "Password123!",
                "Password123!",
                "+380991234567",
                LocalDate.of(2000, 10, 10),
                "male",
                null
        );

        HttpServletResponse response = mock(HttpServletResponse.class);
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(request.email());

        when(userRepository.existsByEmailIgnoreCase(request.email())).thenReturn(false);
        when(userMapper.toUser(request)).thenReturn(user);
        when(jwtService.generateAccessToken(any(), any())).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any(), any())).thenReturn("refresh-token");

        authService.register(request, response);

        verify(userRepository).save(user);
        verify(cookieUtils).addAccessTokenCookie(response, "access-token");
        verify(cookieUtils).addRefreshTokenCookie(response, "refresh-token");
    }

    @Test
    void givenExistingEmail_whenRegister_thenThrowBusinessException() {
        RegistrationRequest request = mock(RegistrationRequest.class);
        when(request.email()).thenReturn("john@example.com");
        when(userRepository.existsByEmailIgnoreCase("john@example.com")).thenReturn(true);

        HttpServletResponse response = mock(HttpServletResponse.class);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> authService.register(request, response));
        assertEquals("EMAIL_ALREADY_EXISTS", ex.getErrorCode().name());
    }

    // endregion


    // region: Login Tests

    @Test
    void givenValidLoginRequestAndExistingUser_whenLogin_thenAuthenticateAndAddCookies() {
        LoginRequest request = new LoginRequest("john@example.com", "Password123!");
        HttpServletResponse response = mock(HttpServletResponse.class);

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(request.email());
        user.setPassword("encodedPassword");

        when(userRepository.findByEmailIgnoreCase(request.email())).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(any(), any())).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any(), any())).thenReturn("refresh-token");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mock(org.springframework.security.core.Authentication.class));

        authService.login(request, response);

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(cookieUtils).addAccessTokenCookie(response, "access-token");
        verify(cookieUtils).addRefreshTokenCookie(response, "refresh-token");
    }

    @Test
    void givenNonExistingUser_whenLogin_thenThrowBusinessException() {
        LoginRequest request = new LoginRequest("john@example.com", "Password123!");
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(userRepository.findByEmailIgnoreCase(request.email())).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> authService.login(request, response));
        assertEquals("USER_NOT_FOUND", ex.getErrorCode().name());
    }

    // endregion


    // region: Refresh Token Tests

    @Test
    void givenValidRefreshToken_whenRefreshAccessToken_thenAddNewAccessTokenCookie() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(cookieUtils.getRefreshToken(request)).thenReturn("refresh-token");
        when(jwtService.refreshAccessToken("refresh-token")).thenReturn("new-access-token");

        authService.refreshAccessToken(request, response);

        verify(cookieUtils).addAccessTokenCookie(response, "new-access-token");
    }

    // endregion


    // region: Logout Tests

    @Test
    void whenLogout_thenClearAccessAndRefreshTokenCookies() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        authService.logout(request, response);

        verify(cookieUtils).clearAccessTokenCookie(response);
        verify(cookieUtils).clearRefreshTokenCookie(response);
    }

    // endregion


    // region: Change Password Tests

    @Test
    void givenValidChangePasswordRequest_whenChangePassword_thenUpdatePassword() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        ChangePasswordRequest changeRequest = new ChangePasswordRequest(
                "OldPassword123!",
                "NewPassword123!",
                "NewPassword123!"
        );

        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);
        user.setPassword("encodedOldPassword");

        when(cookieUtils.getAccessToken(request)).thenReturn("access-token");
        when(jwtService.extractUserId("access-token")).thenReturn(userId.toString());
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("OldPassword123!", "encodedOldPassword")).thenReturn(true);
        when(passwordEncoder.encode("NewPassword123!")).thenReturn("encodedNewPassword");

        authService.changePassword(request, changeRequest);

        assertEquals("encodedNewPassword", user.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    void givenMismatchedPasswords_whenChangePassword_thenThrowBusinessException() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        ChangePasswordRequest changeRequest = new ChangePasswordRequest(
                "OldPassword123!",
                "NewPassword123!",
                "MismatchPassword!"
        );

        BusinessException ex = assertThrows(BusinessException.class,
                () -> authService.changePassword(request, changeRequest));
        assertEquals("PASSWORD_MISMATCH", ex.getErrorCode().name());
    }

    @Test
    void givenNonExistingUser_whenChangePassword_thenThrowBusinessException() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        ChangePasswordRequest changeRequest = new ChangePasswordRequest(
                "OldPassword123!",
                "NewPassword123!",
                "NewPassword123!"
        );

        UUID userId = UUID.randomUUID();

        when(cookieUtils.getAccessToken(request)).thenReturn("access-token");
        when(jwtService.extractUserId("access-token")).thenReturn(userId.toString());
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> authService.changePassword(request, changeRequest));
        assertEquals("USER_NOT_FOUND", ex.getErrorCode().name());
    }

    @Test
    void givenInvalidCurrentPassword_whenChangePassword_thenThrowBusinessException() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        ChangePasswordRequest changeRequest = new ChangePasswordRequest(
                "WrongOldPassword",
                "NewPassword123!",
                "NewPassword123!"
        );

        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);
        user.setPassword("encodedOldPassword");

        when(cookieUtils.getAccessToken(request)).thenReturn("access-token");
        when(jwtService.extractUserId("access-token")).thenReturn(userId.toString());
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongOldPassword", "encodedOldPassword")).thenReturn(false);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> authService.changePassword(request, changeRequest));
        assertEquals("INVALID_CURRENT_PASSWORD", ex.getErrorCode().name());
    }

    // endregion


    // region: Validation Tests for RegistrationRequest

    @Test
    void givenInvalidEmail_whenValidateRegistrationRequest_thenValidationFails() {
        RegistrationRequest request = new RegistrationRequest(
                "John",
                "Doe",
                "invalid-email",
                "Password123!",
                "Password123!",
                "+380991234567",
                LocalDate.of(2000, 1, 1),
                "male",
                null
        );

        Set<ConstraintViolation<RegistrationRequest>> violations = validator.validate(request);

        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("email")
                               && v.getMessage().contains("valid")));
    }

    @Test
    void givenEmptyFirstName_whenValidateRegistrationRequest_thenValidationFails() {
        RegistrationRequest request = new RegistrationRequest(
                "",
                "Doe",
                "john@example.com",
                "Password123!",
                "Password123!",
                "+380991234567",
                LocalDate.of(2000, 1, 1),
                "male",
                null
        );

        Set<ConstraintViolation<RegistrationRequest>> violations = validator.validate(request);

        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("firstName")
                               && v.getMessage().contains("must not be empty")));
    }

    @Test
    void givenPasswordWithoutSpecialCharacter_whenValidateRegistrationRequest_thenValidationFails() {
        RegistrationRequest request = new RegistrationRequest(
                "John",
                "Doe",
                "john@example.com",
                "Password123",
                "Password123",
                "+380991234567",
                LocalDate.of(2000, 1, 1),
                "male",
                null
        );

        Set<ConstraintViolation<RegistrationRequest>> violations = validator.validate(request);

        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("password")
                               && v.getMessage().contains("special character")));
    }

    @Test
    void givenNullDateOfBirth_whenValidateRegistrationRequest_thenValidationFails() {
        RegistrationRequest request = new RegistrationRequest(
                "John",
                "Doe",
                "john@example.com",
                "Password123!",
                "Password123!",
                "+380991234567",
                null,
                "male",
                null
        );

        Set<ConstraintViolation<RegistrationRequest>> violations = validator.validate(request);

        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("dateOfBirth")
                               && v.getMessage().contains("must not be null")));
    }

    @Test
    void givenInvalidGender_whenValidateRegistrationRequest_thenValidationFails() {
        RegistrationRequest request = new RegistrationRequest(
                "John",
                "Doe",
                "john@example.com",
                "Password123!",
                "Password123!",
                "+380991234567",
                LocalDate.of(2000, 1, 1),
                "invalidGender",
                null
        );

        Set<ConstraintViolation<RegistrationRequest>> violations = validator.validate(request);

        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("gender")
                               && v.getMessage().contains("must be 'male', 'female' or 'other'")));
    }

    // endregion


    // region: Validation Tests for LoginRequest

    @Test
    void givenInvalidEmail_whenValidateLoginRequest_thenValidationFails() {
        LoginRequest request = new LoginRequest(
                "invalid-email",
                "Password123!"
        );

        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("email")
                               && v.getMessage().contains("valid")));
    }

    @Test
    void givenBlankEmail_whenValidateLoginRequest_thenValidationFails() {
        LoginRequest request = new LoginRequest(
                "",
                "Password123!"
        );

        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("email")
                               && v.getMessage().contains("must not be empty")));
    }

    @Test
    void givenShortPassword_whenValidateLoginRequest_thenValidationFails() {
        LoginRequest request = new LoginRequest(
                "user@example.com",
                "123"
        );

        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("password")
                               && v.getMessage().contains("at least 6 characters")));
    }

    @Test
    void givenBlankPassword_whenValidateLoginRequest_thenValidationFails() {
        LoginRequest request = new LoginRequest(
                "user@example.com",
                ""
        );

        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("password")
                               && v.getMessage().contains("must not be empty")));
    }

    // endregion


    // region: Validation Tests for ChangePasswordRequest

    @Test
    void givenBlankCurrentPassword_whenValidateChangePasswordRequest_thenValidationFails() {
        ChangePasswordRequest request = new ChangePasswordRequest(
                "",
                "NewStrongPassword1@",
                "NewStrongPassword1@"
        );

        Set<ConstraintViolation<ChangePasswordRequest>> violations = validator.validate(request);

        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("currentPassword")
                               && v.getMessage().contains("must not be empty")));
    }

    @Test
    void givenWeakNewPassword_whenValidateChangePasswordRequest_thenValidationFails() {
        ChangePasswordRequest request = new ChangePasswordRequest(
                "OldPassword123!",
                "weakpass",
                "weakpass"
        );

        Set<ConstraintViolation<ChangePasswordRequest>> violations = validator.validate(request);

        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("newPassword")
                               && v.getMessage().contains("uppercase letter")));
    }

    @Test
    void givenBlankNewPassword_whenValidateChangePasswordRequest_thenValidationFails() {
        ChangePasswordRequest request = new ChangePasswordRequest(
                "OldPassword123!",
                "",
                ""
        );

        Set<ConstraintViolation<ChangePasswordRequest>> violations = validator.validate(request);

        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("newPassword")
                               && v.getMessage().contains("must not be empty")));
    }

    @Test
    void givenBlankConfirmNewPassword_whenValidateChangePasswordRequest_thenValidationFails() {
        ChangePasswordRequest request = new ChangePasswordRequest(
                "OldPassword123!",
                "NewStrongPassword1@",
                ""
        );

        Set<ConstraintViolation<ChangePasswordRequest>> violations = validator.validate(request);

        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("confirmNewPassword")
                               && v.getMessage().contains("must not be empty")));
    }

    // endregion
}