package org.example.authservice.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CookieUtils {
    private static final String ACCESS_TOKEN_COOKIE = "access_token";

    @Value("${app.security.jwt.access-token-expiration}")
    private Long accessTokenExpiration;

    public void addJwtCookies(HttpServletResponse response, String accessToken) {
        Cookie accessCookie = new Cookie(ACCESS_TOKEN_COOKIE, accessToken);
        accessCookie.setHttpOnly(true);
        accessCookie.setSecure(true);
        accessCookie.setPath("/");
        accessCookie.setMaxAge((int) (accessTokenExpiration / 1000));

        response.addCookie(accessCookie);
    }
}
