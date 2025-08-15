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
import org.example.authservice.grpc.UserProfileServiceGrpcClient;
import org.example.authservice.mapper.UserMapper;
import org.example.authservice.model.Role;
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
import user.profile.*;

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
    private UserProfileServiceGrpcClient userProfileServiceGrpcClient;

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
        userProfileServiceGrpcClient = mock(UserProfileServiceGrpcClient.class);

        authService = new AuthServiceImpl(
                userMapper,
                jwtService,
                cookieUtils,
                userRepository,
                passwordEncoder,
                authenticationManager,
                userProfileServiceGrpcClient
        );

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // region: Registration Tests

    @Test
    void givenValidRegistrationRequest_whenRegister_thenSaveUserAndAddRefreshTokenCookie() {
        RegistrationRequest request = new RegistrationRequest(
                "john_doe",
                "john@example.com",
                "Password123!",
                "Password123!",
                "+380991234567",
                LocalDate.of(2000, 10, 10),
                0L
        );

        HttpServletResponse response = mock(HttpServletResponse.class);

        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setEmail(request.email());
        user.setRole(Role.USER);

        when(userRepository.existsByEmailIgnoreCase(request.email())).thenReturn(false);

        when(userMapper.toUser(request)).thenReturn(user);

        when(userProfileServiceGrpcClient.checkPhoneNumber(any(CheckPhoneNumberRequest.class)))
                .thenReturn(CheckPhoneNumberResponse.newBuilder().setIsUnique(true).build());

        when(jwtService.generateAccessToken(any(), any())).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any(), any())).thenReturn("refresh-token");

        when(userProfileServiceGrpcClient.createUserProfile(any(CreateUserProfileRequest.class)))
                .thenReturn(CreateUserProfileResponse.newBuilder()
                        .setUsername("john_doe")
                        .setAvatarKey("avatar.png")
                        .build());

        var authResponse = authService.register(request, response);

        verify(userRepository).save(user);
        verify(cookieUtils).addRefreshTokenCookie(response, "refresh-token");

        assertEquals("access-token", authResponse.accessToken());
        assertEquals("USER", authResponse.role());
        assertEquals("john_doe", authResponse.profile().username());
        assertEquals("avatar.png", authResponse.profile().avatarKey());
    }

    // endregion


    // region: Login Tests

    @Test
    void givenValidLoginRequestAndExistingUser_whenLogin_thenAuthenticateAndAddRefreshTokenCookie() {
        LoginRequest request = new LoginRequest("john@example.com", "Password123!");
        HttpServletResponse response = mock(HttpServletResponse.class);

        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setEmail(request.email());
        user.setPassword("encodedPassword");
        user.setRole(Role.USER);

        when(userRepository.findByEmailIgnoreCase(request.email())).thenReturn(Optional.of(user));

        when(jwtService.generateAccessToken(any(), any())).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any(), any())).thenReturn("refresh-token");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mock(org.springframework.security.core.Authentication.class));

        GetUserProfileInfoResponse profileResponse = GetUserProfileInfoResponse.newBuilder()
                .setUsername("john_doe")
                .setAvatarKey("avatar.png")
                .build();
        when(userProfileServiceGrpcClient.getUserProfileInfo(any())).thenReturn(profileResponse);

        var authResponse = authService.login(request, response);

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(cookieUtils).addRefreshTokenCookie(response, "refresh-token");

        assertEquals("access-token", authResponse.accessToken());
        assertEquals("USER", authResponse.role());
        assertEquals("john_doe", authResponse.profile().username());
        assertEquals("avatar.png", authResponse.profile().avatarKey());
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
    void givenValidRefreshToken_whenRefreshAccessToken_thenReturnNewAccessToken() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);
        user.setRole(Role.USER);
        user.setEmail("test@example.com");

        when(cookieUtils.getRefreshToken(request)).thenReturn("refresh-token");
        when(jwtService.refreshAccessToken("refresh-token")).thenReturn("new-access-token");
        when(jwtService.extractUserId("new-access-token")).thenReturn(userId.toString());
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        GetUserProfileInfoResponse profileResponse = GetUserProfileInfoResponse.newBuilder()
                .setUsername("testUser")
                .setAvatarKey("avatar.png")
                .build();
        when(userProfileServiceGrpcClient.getUserProfileInfo(any())).thenReturn(profileResponse);

        var authResponse = authService.refreshAccessToken(request, response);

        assertEquals("new-access-token", authResponse.accessToken());
        assertEquals("USER", authResponse.role());
        assertEquals("testUser", authResponse.profile().username());
        assertEquals("avatar.png", authResponse.profile().avatarKey());
    }

    @Test
    void givenMissingRefreshToken_whenRefreshAccessToken_thenThrowBusinessException() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(cookieUtils.getRefreshToken(request)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> authService.refreshAccessToken(request, response));
        assertEquals("REFRESH_TOKEN_MISSED", ex.getErrorCode().name());
    }

    // endregion


    // region: Logout Tests

    @Test
    void whenLogout_thenClearRefreshTokenCookie() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        authService.logout(request, response);

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

        when(jwtService.extractUserId(request)).thenReturn(userId);
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

        when(jwtService.extractUserId(request)).thenReturn(userId);
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

        when(jwtService.extractUserId(request)).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongOldPassword", "encodedOldPassword")).thenReturn(false);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> authService.changePassword(request, changeRequest));
        assertEquals("INVALID_CURRENT_PASSWORD", ex.getErrorCode().name());
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
                               && v.getMessage().contains("Електронна пошта повинна бути валідною")));
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
                               && v.getMessage().contains("Електронна пошта не повинна бути порожньою")));
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
                               && v.getMessage().contains("Пароль повинен містити щонайменше 6 символів")));
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
                               && v.getMessage().contains("Пароль не може бути порожнім")));
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
                               && v.getMessage().contains("не може бути порожнім")));
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
                               && v.getMessage().contains("щонайменше одну велику літеру")));
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
                               && v.getMessage().contains("не може бути порожнім")));
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
                               && v.getMessage().contains("не може бути порожнім")));
    }

    // endregion
}