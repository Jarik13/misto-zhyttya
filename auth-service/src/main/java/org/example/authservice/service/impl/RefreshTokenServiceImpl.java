package org.example.authservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.authservice.model.RefreshToken;
import org.example.authservice.model.User;
import org.example.authservice.repository.RefreshTokenRepository;
import org.example.authservice.security.JwtService;
import org.example.authservice.service.RefreshTokenService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public void createRefreshToken(User user) {
        String token = jwtService.generateRefreshToken(user.getEmail(), user.getId().toString());

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(token)
                .expiryDate(Instant.now().plusSeconds(jwtService.getRefreshTokenExpiration()))
                .build();

        refreshTokenRepository.save(refreshToken);
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    @Override
    public void deleteByUserId(UUID userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }
}
