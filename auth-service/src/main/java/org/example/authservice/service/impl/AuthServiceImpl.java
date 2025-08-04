package org.example.authservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.authservice.dto.auth.LoginRequest;
import org.example.authservice.dto.auth.RegistrationRequest;
import org.example.authservice.mapper.UserMapper;
import org.example.authservice.repository.UserRepository;
import org.example.authservice.security.CookieUtils;
import org.example.authservice.security.JwtService;
import org.example.authservice.service.AuthService;
import org.example.authservice.service.RefreshTokenService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserMapper userMapper;
    private final JwtService jwtService;
    private final CookieUtils cookieUtils;
    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager;

    @Override
    public void register(RegistrationRequest registrationRequest) {

    }

    @Override
    public void login(LoginRequest loginRequest) {

    }

    @Override
    public void logout() {

    }
}
