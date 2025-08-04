package org.example.authservice.security;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.PrivateKey;
import java.security.PublicKey;

@Service
@RequiredArgsConstructor
public class JwtService {
    private final KeyUtils keyUtils;

    private static final String TOKEN_TYPE = "token_type";
    private static final String USER_ID = "user_id";

    private PrivateKey privateKey;
    private PublicKey publicKey;

    @Value("${app.security.jwt.access-token-expiration}")
    private Long accessTokenExpiration;

    @Value("${app.security.jwt.refresh-token-expiration}")
    private Long refreshTokenExpiration;

    @PostConstruct
    public void initKeys() {
        this.privateKey = keyUtils.getPrivateKey();
        this.publicKey = keyUtils.getPublicKey();
    }
}
