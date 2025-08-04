package org.example.authservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtFilterTests {
    @Mock
    private CookieUtils cookieUtils;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private FilterChain filterChain;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private JwtFilter jwtFilter;

    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        jwtFilter = new JwtFilter(cookieUtils, jwtService, userDetailsService);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
        SecurityContextHolder.clearContext();
    }

    @Test
    void whenServletPathContainsAuth_thenFilterChainCalledAndNoAuthenticationSet() throws Exception {
        when(request.getServletPath()).thenReturn("/auth/login");

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void whenAuthorizationHeaderMissing_thenFilterChainCalledAndNoAuthenticationSet() throws Exception {
        when(request.getServletPath()).thenReturn("/api/data");
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void whenAuthorizationHeaderNotStartingWithBearer_thenFilterChainCalledAndNoAuthenticationSet() throws Exception {
        when(request.getServletPath()).thenReturn("/api/data");
        when(request.getHeader("Authorization")).thenReturn("Basic 1234");

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void whenAccessTokenIsNull_thenFilterChainCalledAndNoAuthenticationSet() throws Exception {
        when(request.getServletPath()).thenReturn("/api/data");
        when(request.getHeader("Authorization")).thenReturn("Bearer token");
        when(cookieUtils.getAccessToken(request)).thenReturn(null);

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void whenEmailIsNull_thenFilterChainCalledAndNoAuthenticationSet() throws Exception {
        when(request.getServletPath()).thenReturn("/api/data");
        when(request.getHeader("Authorization")).thenReturn("Bearer token");
        when(cookieUtils.getAccessToken(request)).thenReturn("token");
        when(jwtService.extractEmail("token")).thenReturn(null);

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void whenAuthenticationAlreadySet_thenFilterChainCalledAndNoChange() throws Exception {
        when(request.getServletPath()).thenReturn("/api/data");
        when(request.getHeader("Authorization")).thenReturn("Bearer token");
        when(cookieUtils.getAccessToken(request)).thenReturn("token");
        when(jwtService.extractEmail("token")).thenReturn("user@example.com");

        UsernamePasswordAuthenticationToken existingAuth =
                new UsernamePasswordAuthenticationToken("user@example.com", null, null);
        SecurityContextHolder.getContext().setAuthentication(existingAuth);

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertEquals(existingAuth, SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void whenTokenInvalid_thenFilterChainCalledAndNoAuthenticationSet() throws Exception {
        when(request.getServletPath()).thenReturn("/api/data");
        when(request.getHeader("Authorization")).thenReturn("Bearer token");
        when(cookieUtils.getAccessToken(request)).thenReturn("token");
        when(jwtService.extractEmail("token")).thenReturn("user@example.com");
        when(jwtService.isTokenValid("token", "user@example.com")).thenReturn(false);

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void whenValidToken_thenAuthenticationIsSetAndFilterChainCalled() throws Exception {
        when(request.getServletPath()).thenReturn("/api/data");
        when(request.getHeader("Authorization")).thenReturn("Bearer token");
        when(cookieUtils.getAccessToken(request)).thenReturn("token");
        when(jwtService.extractEmail("token")).thenReturn("user@example.com");

        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getAuthorities()).thenReturn(Collections.emptyList());
        when(userDetailsService.loadUserByUsername("user@example.com")).thenReturn(userDetails);

        when(jwtService.isTokenValid("token", "user@example.com")).thenReturn(true);

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals("user@example.com", authentication.getPrincipal());
        assertNull(authentication.getCredentials());

        assertEquals(Collections.emptyList(), authentication.getAuthorities());
    }

    @Test
    void whenJwtServiceThrowsExceptionDuringExtractEmail_thenFilterChainCalledAndNoAuthenticationSet() throws Exception {
        when(request.getServletPath()).thenReturn("/api/data");
        when(request.getHeader("Authorization")).thenReturn("Bearer token");
        when(cookieUtils.getAccessToken(request)).thenReturn("token");

        when(jwtService.extractEmail("token")).thenThrow(new RuntimeException("JWT parsing error"));

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void whenUserDetailsServiceThrowsException_thenFilterChainCalledAndNoAuthenticationSet() throws Exception {
        when(request.getServletPath()).thenReturn("/api/data");
        when(request.getHeader("Authorization")).thenReturn("Bearer token");
        when(cookieUtils.getAccessToken(request)).thenReturn("token");
        when(jwtService.extractEmail("token")).thenReturn("user@example.com");
        when(jwtService.isTokenValid("token", "user@example.com")).thenReturn(true);

        when(userDetailsService.loadUserByUsername("user@example.com")).thenThrow(new RuntimeException("User not found"));

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void whenAuthoritiesIsNull_thenAuthenticationAuthoritiesIsEmpty() throws Exception {
        when(request.getServletPath()).thenReturn("/api/data");
        when(request.getHeader("Authorization")).thenReturn("Bearer token");
        when(cookieUtils.getAccessToken(request)).thenReturn("token");
        when(jwtService.extractEmail("token")).thenReturn("user@example.com");

        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getAuthorities()).thenReturn(null);
        when(userDetailsService.loadUserByUsername("user@example.com")).thenReturn(userDetails);

        when(jwtService.isTokenValid("token", "user@example.com")).thenReturn(true);

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals("user@example.com", authentication.getPrincipal());
        assertNull(authentication.getCredentials());

        assertNotNull(authentication.getAuthorities());
    }

}
