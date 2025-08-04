package org.example.authservice.security;

import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

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


    public String generateAccessToken(String subject, String userId) {
        Map<String, Object> claims = Map.of(
                TOKEN_TYPE, "access_token",
                USER_ID, userId
        );
        return buildToken(subject, claims, accessTokenExpiration);
    }


    private String buildToken(String subject, Map<String, Object> claims, Long expiration) {
        return Jwts.builder()
                .subject(subject)
                .claims(claims)
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusSeconds(expiration)))
                .signWith(privateKey)
                .compact();
    }
}
