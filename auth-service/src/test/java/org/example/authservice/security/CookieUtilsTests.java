package org.example.authservice.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CookieUtilsTests {
    private CookieUtils cookieUtils;

    @BeforeEach
    void setUp() {
        cookieUtils = new CookieUtils();
        ReflectionTestUtils.setField(cookieUtils, "refreshTokenExpiration", 7200000L); // 2 hours
    }

    // region: Refresh Token - Add

    @Test
    void givenRefreshToken_whenAddRefreshTokenCookie_thenCookieIsAddedWithCorrectAttributes() {
        HttpServletResponse response = mock(HttpServletResponse.class);
        String token = "testRefreshToken";

        cookieUtils.addRefreshTokenCookie(response, token);

        ArgumentCaptor<Cookie> captor = ArgumentCaptor.forClass(Cookie.class);
        verify(response).addCookie(captor.capture());

        Cookie cookie = captor.getValue();
        assertEquals("refresh_token", cookie.getName());
        assertEquals(token, cookie.getValue());
        assertEquals(7200, cookie.getMaxAge());
        assertTrue(cookie.isHttpOnly());
        assertFalse(cookie.getSecure());
        assertEquals("/", cookie.getPath());
    }

    @Test
    void givenNullResponse_whenAddRefreshTokenCookie_thenThrowsException() {
        HttpServletResponse response = null;
        String token = "testRefreshToken";

        assertThrows(NullPointerException.class, () -> cookieUtils.addRefreshTokenCookie(response, token));
    }

    // endregion


    // region: Refresh Token - Clear

    @Test
    void givenNoValue_whenClearRefreshTokenCookie_thenCookieIsCleared() {
        HttpServletResponse response = mock(HttpServletResponse.class);

        cookieUtils.clearRefreshTokenCookie(response);

        ArgumentCaptor<Cookie> captor = ArgumentCaptor.forClass(Cookie.class);
        verify(response).addCookie(captor.capture());

        Cookie cookie = captor.getValue();
        assertEquals("refresh_token", cookie.getName());
        assertNull(cookie.getValue());
        assertEquals(0, cookie.getMaxAge());
    }

    @Test
    void givenNullResponse_whenClearRefreshTokenCookie_thenThrowsException() {
        HttpServletResponse response = null;

        assertThrows(NullPointerException.class, () -> cookieUtils.clearRefreshTokenCookie(response));
    }

    // endregion


    // region: Refresh Token - Get

    @Test
    void givenRefreshTokenCookieExists_whenGetRefreshToken_thenReturnsTokenValue() {
        Cookie[] cookies = { new Cookie("refresh_token", "refresh123") };
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(cookies);

        String token = cookieUtils.getRefreshToken(request);

        assertEquals("refresh123", token);
    }

    @Test
    void givenEmptyCookiesArray_whenGetRefreshToken_thenReturnsNull() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(new Cookie[0]);

        String token = cookieUtils.getRefreshToken(request);

        assertNull(token);
    }

    @Test
    void givenNullRequest_whenGetRefreshToken_thenThrowsException() {
        HttpServletRequest request = null;

        assertThrows(NullPointerException.class, () -> cookieUtils.getRefreshToken(request));
    }

    // endregion
}
