package org.example.authservice.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.authservice.dto.auth.ChangePasswordRequest;
import org.example.authservice.dto.auth.LoginRequest;
import org.example.authservice.dto.auth.RegistrationRequest;
import org.example.authservice.dto.error.ErrorCode;
import org.example.authservice.exception.BusinessException;
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

    @Override
    @Transactional
    public void register(RegistrationRequest request, HttpServletResponse response) {
        checkUserEmail(request.email());
        checkPasswords(request.password(), request.confirmPassword());

        User user = userMapper.toUser(request);
        userRepository.save(user);

        String accessToken = jwtService.generateAccessToken(user.getEmail(), user.getId().toString());
        String refreshToken = jwtService.generateRefreshToken(user.getEmail(), user.getId().toString());

        cookieUtils.addAccessTokenCookie(response, accessToken);
        cookieUtils.addRefreshTokenCookie(response, refreshToken);
    }

    @Override
    @Transactional
    public void login(LoginRequest request, HttpServletResponse response) {
        var authToken = new UsernamePasswordAuthenticationToken(request.email(), request.password());
        authenticationManager.authenticate(authToken);

        var user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        String accessToken = jwtService.generateAccessToken(user.getEmail(), user.getId().toString());
        String refreshToken = jwtService.generateRefreshToken(user.getEmail(), user.getId().toString());

        cookieUtils.addAccessTokenCookie(response, accessToken);
        cookieUtils.addRefreshTokenCookie(response, refreshToken);
    }

    @Override
    public void refreshAccessToken(HttpServletRequest request, HttpServletResponse response) {
        cookieUtils.addAccessTokenCookie(
                response,
                jwtService.refreshAccessToken(cookieUtils.getRefreshToken(request))
        );
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        cookieUtils.clearAccessTokenCookie(response);
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

    private UUID extractUserIdFromAccessToken(HttpServletRequest request) {
        String token = cookieUtils.getAccessToken(request);
        if (token == null || token.isEmpty()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return UUID.fromString(jwtService.extractUserId(token));
    }
}