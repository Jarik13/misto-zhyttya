package org.example.authservice.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CookieUtils {
    private static final String ACCESS_TOKEN_COOKIE = "access_token";

    @Value("${app.security.jwt.access-token-expiration}")
    private Long accessTokenExpiration;
}
