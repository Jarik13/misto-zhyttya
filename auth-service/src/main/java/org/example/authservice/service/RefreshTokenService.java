package org.example.authservice.service;

import org.example.authservice.model.RefreshToken;
import org.example.authservice.model.User;

import java.util.Optional;

public interface RefreshTokenService {
    void createRefreshToken(User user);
    Optional<RefreshToken> findByToken(String token);
    void deleteByUser(User user);
}
