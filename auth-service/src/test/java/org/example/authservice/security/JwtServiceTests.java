package org.example.authservice.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtServiceTests {
    private JwtService jwtService;
    private KeyUtils keyUtils;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    @BeforeEach
    void setUp() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        privateKey = keyPair.getPrivate();
        publicKey = keyPair.getPublic();

        keyUtils = mock(KeyUtils.class);
        when(keyUtils.getPrivateKey()).thenReturn(privateKey);
        when(keyUtils.getPublicKey()).thenReturn(publicKey);

        jwtService = new JwtService(keyUtils);

        setField(jwtService, "accessTokenExpiration", Duration.ofSeconds(2).getSeconds());
        setField(jwtService, "refreshTokenExpiration", Duration.ofSeconds(4).getSeconds());

        jwtService.initKeys();
    }

    // region: Access Token Tests

    @Test
    void givenValidUserData_whenGenerateAccessToken_thenShouldReturnValidJwt() {
        String token = jwtService.generateAccessToken("user@example.com", "123");

        assertNotNull(token);
        assertTrue(jwtService.isTokenValid(token, "user@example.com"));
        assertEquals("123", jwtService.extractUserId(token));
        assertEquals("user@example.com", jwtService.extractEmail(token));
    }

    @Test
    void givenExpiredAccessToken_whenIsTokenValid_thenShouldReturnFalse() throws InterruptedException {
        String token = jwtService.generateAccessToken("user@example.com", "123");

        Thread.sleep(3000);

        assertFalse(jwtService.isTokenValid(token, "user@example.com"));
    }

    @Test
    void givenEmptyUserId_whenGenerateAccessToken_thenShouldThrowException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> jwtService.generateAccessToken("user@example.com", ""));
        assertEquals("User ID must not be null or empty", ex.getMessage());
    }

    @Test
    void givenNullUserId_whenGenerateAccessToken_thenShouldThrowException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> jwtService.generateAccessToken("user@example.com", null));
        assertEquals("User ID must not be null or empty", ex.getMessage());
    }

    @Test
    void givenNullSubject_whenGenerateAccessToken_thenShouldThrowException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> jwtService.generateAccessToken(null, "123"));
        assertEquals("Subject (email) must not be null or empty", ex.getMessage());
    }

    @Test
    void givenEmptySubject_whenGenerateAccessToken_thenShouldThrowException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> jwtService.generateAccessToken("", "123"));
        assertEquals("Subject (email) must not be null or empty", ex.getMessage());
    }

    // endregion


    // region: Refresh Token Tests

    @Test
    void givenValidRefreshToken_whenRefreshAccessToken_thenShouldReturnNewValidAccessToken() {
        String refreshToken = jwtService.generateRefreshToken("user@example.com", "123");

        String newAccessToken = jwtService.refreshAccessToken(refreshToken);

        assertNotNull(newAccessToken);
        assertTrue(jwtService.isTokenValid(newAccessToken, "user@example.com"));
        assertEquals("123", jwtService.extractUserId(newAccessToken));
    }

    @Test
    void givenExpiredRefreshToken_whenRefreshAccessToken_thenShouldThrowException() throws InterruptedException {
        String refreshToken = jwtService.generateRefreshToken("user@example.com", "123");

        Thread.sleep(5000);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> jwtService.refreshAccessToken(refreshToken));
        assertTrue(ex.getMessage().contains("Token has expired"));
    }

    @Test
    void givenAccessTokenInsteadOfRefreshToken_whenRefreshAccessToken_thenShouldThrowException() {
        String accessToken = jwtService.generateAccessToken("user@example.com", "123");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> jwtService.refreshAccessToken(accessToken));

        assertEquals("Invalid refresh token", ex.getMessage());
    }

    @Test
    void givenNullSubject_whenGenerateRefreshToken_thenShouldThrowException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> jwtService.generateRefreshToken(null, "123"));
        assertEquals("Subject (email) must not be null or empty", ex.getMessage());
    }

    @Test
    void givenEmptySubject_whenGenerateRefreshToken_thenShouldThrowException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> jwtService.generateRefreshToken("", "123"));
        assertEquals("Subject (email) must not be null or empty", ex.getMessage());
    }

    @Test
    void givenEmptyUserId_whenGenerateRefreshToken_thenShouldThrowException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> jwtService.generateRefreshToken("user@example.com", ""));
        assertEquals("User ID must not be null or empty", ex.getMessage());
    }

    @Test
    void givenNullUserId_whenGenerateRefreshToken_thenShouldThrowException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> jwtService.generateRefreshToken("user@example.com", null));
        assertEquals("User ID must not be null or empty", ex.getMessage());
    }

    // endregion


    // region: Token Validation Tests

    @Test
    void givenInvalidJwtString_whenIsTokenValid_thenShouldReturnFalse() {
        assertFalse(jwtService.isTokenValid("invalid.token.here", "user@example.com"));
    }

    @Test
    void givenNullToken_whenIsTokenValid_thenShouldReturnFalse() {
        assertFalse(jwtService.isTokenValid(null, "user@example.com"));
    }

    @Test
    void givenEmailMismatch_whenIsTokenValid_thenShouldReturnFalse() {
        String token = jwtService.generateAccessToken("user@example.com", "123");
        assertFalse(jwtService.isTokenValid(token, "wrong@example.com"));
    }

    // endregion

    private static void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
