package org.example.authservice.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.authservice.grpc.UserProfileServiceGrpcClient;
import org.example.authservice.model.AuthProvider;
import org.example.authservice.model.Role;
import org.example.authservice.model.User;
import org.example.authservice.repository.UserRepository;
import org.example.authservice.security.provider.OAuth2UserInfoProcessorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.RedirectStrategy;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomOAuth2SuccessHandlerTests {
    @Mock
    private CookieUtils cookieUtils;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private OAuth2UserInfoProcessorFactory processorFactory;

    @Mock
    private UserProfileServiceGrpcClient userProfileServiceGrpcClient;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private OAuth2AuthenticationToken authentication;

    @Mock
    private OAuth2User oAuth2User;

    @InjectMocks
    private CustomOAuth2SuccessHandler successHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        successHandler = new CustomOAuth2SuccessHandler(cookieUtils, userRepository, jwtService, processorFactory, userProfileServiceGrpcClient);
    }

    @Test
    void givenNewUser_whenAuthenticationSuccess_thenUserIsSavedAndCookiesAdded() throws IOException, ServletException {
        String email = "test@example.com";
        String registrationId = "GITHUB";
        UUID userId = UUID.randomUUID();

        User savedUser = User.builder()
                .id(userId)
                .email(email)
                .enabled(true)
                .provider(AuthProvider.GITHUB)
                .role(Role.USER)
                .build();

        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(authentication.getAuthorizedClientRegistrationId()).thenReturn(registrationId.toLowerCase());
        when(oAuth2User.getAttribute("email")).thenReturn(email);
        when(userRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateAccessToken(eq(email), eq(userId.toString()))).thenReturn("access-token");
        when(jwtService.generateRefreshToken(eq(email), eq(userId.toString()))).thenReturn("refresh-token");

        RedirectStrategy mockRedirectStrategy = mock(RedirectStrategy.class);
        successHandler.setRedirectStrategy(mockRedirectStrategy);

        successHandler.onAuthenticationSuccess(request, response, authentication);

        verify(userRepository).save(argThat(user ->
                user.getEmail().equals(email) &&
                user.getProvider() == AuthProvider.GITHUB &&
                user.getRole() == Role.USER
        ));

        verify(cookieUtils).addRefreshTokenCookie(response, "refresh-token");

        verify(mockRedirectStrategy).sendRedirect(request, response, "http://localhost:5173");
    }

    @Test
    void givenExistingUser_whenAuthenticationSuccess_thenUserIsNotSavedAgain() throws IOException, ServletException {
        String email = "existing@example.com";
        String registrationId = "GOOGLE";
        UUID userId = UUID.randomUUID();

        User existingUser = User.builder()
                .id(userId)
                .email(email)
                .enabled(true)
                .provider(AuthProvider.GOOGLE)
                .role(Role.USER)
                .build();

        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(authentication.getAuthorizedClientRegistrationId()).thenReturn(registrationId.toLowerCase());
        when(oAuth2User.getAttribute("email")).thenReturn(email);
        when(userRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.of(existingUser));
        when(jwtService.generateAccessToken(eq(email), eq(userId.toString()))).thenReturn("access-token");
        when(jwtService.generateRefreshToken(eq(email), eq(userId.toString()))).thenReturn("refresh-token");

        RedirectStrategy mockRedirectStrategy = mock(RedirectStrategy.class);
        successHandler.setRedirectStrategy(mockRedirectStrategy);

        successHandler.onAuthenticationSuccess(request, response, authentication);

        verify(userRepository, never()).save(any());
        verify(cookieUtils).addRefreshTokenCookie(response, "refresh-token");

        verify(mockRedirectStrategy).sendRedirect(request, response, "http://localhost:5173");
    }

    @Test
    void givenMissingEmail_whenAuthenticationSuccess_thenThrowsException() {
        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttribute("email")).thenReturn(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                successHandler.onAuthenticationSuccess(request, response, authentication)
        );

        assertEquals("Email not found from OAuth2 provider", exception.getMessage());
    }

    @Test
    void givenUnknownRegistrationId_whenAuthenticationSuccess_thenUsesLocalProvider() throws IOException, ServletException {
        String email = "test@example.com";
        String registrationId = "UNKNOWN";
        UUID userId = UUID.randomUUID();

        User user = User.builder()
                .id(userId)
                .email(email)
                .provider(AuthProvider.LOCAL)
                .enabled(true)
                .role(Role.USER)
                .build();

        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(authentication.getAuthorizedClientRegistrationId()).thenReturn(registrationId);
        when(oAuth2User.getAttribute("email")).thenReturn(email);
        when(userRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtService.generateAccessToken(any(), any())).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any(), any())).thenReturn("refresh-token");

        RedirectStrategy mockRedirectStrategy = mock(RedirectStrategy.class);
        successHandler.setRedirectStrategy(mockRedirectStrategy);

        successHandler.onAuthenticationSuccess(request, response, authentication);

        verify(cookieUtils).addRefreshTokenCookie(response, "refresh-token");
        verify(mockRedirectStrategy).sendRedirect(request, response, "http://localhost:5173");
    }
}
