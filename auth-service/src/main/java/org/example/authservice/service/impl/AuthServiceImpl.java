package org.example.authservice.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.authservice.dto.auth.AuthResponse;
import org.example.authservice.dto.auth.ChangePasswordRequest;
import org.example.authservice.dto.auth.LoginRequest;
import org.example.authservice.dto.auth.RegistrationRequest;
import org.example.authservice.dto.error.ErrorCode;
import org.example.authservice.exception.BusinessException;
import org.example.authservice.grpc.UserProfileServiceGrpcClient;
import org.example.authservice.mapper.UserMapper;
import org.example.authservice.model.User;
import org.example.authservice.repository.UserRepository;
import org.example.authservice.security.CookieUtils;
import org.example.authservice.security.JwtService;
import org.example.authservice.service.AuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import user.profile.CheckPhoneNumberRequest;
import user.profile.CreateUserProfileRequest;

import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserMapper userMapper;
    private final JwtService jwtService;
    private final CookieUtils cookieUtils;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserProfileServiceGrpcClient userProfileServiceGrpcClient;

    @Override
    @Transactional
    public AuthResponse register(RegistrationRequest request, HttpServletResponse response) {
        checkUserEmail(request.email());
        checkPasswords(request.password(), request.confirmPassword());
        checkPhoneNumber(request.phoneNumber());

        User user = userMapper.toUser(request);
        userRepository.save(user);

        String avatarUrl = request.avatarUrl();
        CreateUserProfileRequest userProfileRequest = CreateUserProfileRequest.newBuilder()
                .setUsername(request.username())
                .setPhoneNumber(request.phoneNumber())
                .setDateOfBirth(request.dateOfBirth().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                .setGenderId(request.genderId())
                .setAvatarUrl(avatarUrl != null ? avatarUrl : "")
                .setUserId(String.valueOf(user.getId()))
                .build();

        userProfileServiceGrpcClient.createUserProfile(userProfileRequest);

        String accessToken = jwtService.generateAccessToken(user.getEmail(), user.getId().toString());
        String refreshToken = jwtService.generateRefreshToken(user.getEmail(), user.getId().toString());

        cookieUtils.addRefreshTokenCookie(response, refreshToken);

        return new AuthResponse(accessToken);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request, HttpServletResponse response) {
        var authToken = new UsernamePasswordAuthenticationToken(request.email(), request.password());
        authenticationManager.authenticate(authToken);

        var user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        String accessToken = jwtService.generateAccessToken(user.getEmail(), user.getId().toString());
        String refreshToken = jwtService.generateRefreshToken(user.getEmail(), user.getId().toString());

        cookieUtils.addRefreshTokenCookie(response, refreshToken);

        return new AuthResponse(accessToken);
    }

    @Override
    public AuthResponse refreshAccessToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = cookieUtils.getRefreshToken(request);
        if (refreshToken == null || refreshToken.isEmpty()) {
            log.warn("Refresh token is missing in the request cookies");
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_MISSED);
        }
        return new AuthResponse(jwtService.refreshAccessToken(refreshToken));
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        cookieUtils.clearRefreshTokenCookie(response);
    }

    @Override
    @Transactional
    public void changePassword(HttpServletRequest httpRequest, ChangePasswordRequest request) {
        checkPasswords(request.newPassword(), request.confirmNewPassword());

        UUID userId = extractUserIdFromAccessToken(httpRequest);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, userId));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CURRENT_PASSWORD);
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    @Override
    public String validateToken(String token) {
        String actualToken = token != null && token.startsWith("Bearer ") ?
                token.substring(7) : token;

        if (actualToken != null && jwtService.isTokenValid(actualToken, jwtService.extractEmail(actualToken))) {
            return jwtService.extractUserId(actualToken);
        }
        throw new BusinessException(ErrorCode.INVALID_TOKEN);
    }

    private void checkUserEmail(String email) {
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
    }

    private void checkPasswords(String password, String confirmPassword) {
        if (password == null || !password.equals(confirmPassword)) {
            throw new BusinessException(ErrorCode.PASSWORD_MISMATCH);
        }
    }

    private void checkPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            throw new BusinessException(ErrorCode.PHONE_IS_EMPTY);
        }

        boolean isUnique = userProfileServiceGrpcClient.checkPhoneNumber(
                        CheckPhoneNumberRequest.newBuilder().setPhoneNumber(phoneNumber).build())
                .getIsUnique();

        if (!isUnique) {
            throw new BusinessException(ErrorCode.PHONE_ALREADY_EXISTS);
        }
    }

    private UUID extractUserIdFromAccessToken(HttpServletRequest request) {
        return jwtService.extractUserId(request);
    }
}