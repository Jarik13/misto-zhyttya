package org.example.authservice.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.authservice.grpc.UserProfileServiceGrpcClient;
import org.example.authservice.model.AuthProvider;
import org.example.authservice.model.Role;
import org.example.authservice.model.User;
import org.example.authservice.repository.UserRepository;
import org.example.authservice.security.provider.OAuth2UserInfoProcessor;
import org.example.authservice.security.provider.OAuth2UserInfoProcessorFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import user.profile.CreateUserProfileRequest;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final CookieUtils cookieUtils;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final OAuth2UserInfoProcessorFactory processorFactory;
    private final UserProfileServiceGrpcClient userProfileServiceGrpcClient;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        if (email == null) {
            throw new IllegalArgumentException("Email not found from OAuth2 provider");
        }

        String registrationId = ((OAuth2AuthenticationToken) authentication)
                .getAuthorizedClientRegistrationId();

        var processor = processorFactory.getProcessor(registrationId);

        Map<String, Object> profileAttrs = processor.extractAttributes(oAuth2User);

        String username = (String) profileAttrs.getOrDefault("username", email.substring(0, email.indexOf("@")));
        String phoneNumber = (String) profileAttrs.getOrDefault("phoneNumber", "");
        String dateOfBirth = (String) profileAttrs.getOrDefault("dateOfBirth", "");
        Long genderId = profileAttrs.get("genderId") != null ? Long.parseLong(profileAttrs.get("genderId").toString()) : 3L;
        String avatarUrl = (String) profileAttrs.getOrDefault("avatarUrl", "");

        var appUser = userRepository.findByEmailIgnoreCase(email)
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .email(email)
                                .password(null)
                                .role(Role.USER)
                                .provider(AuthProvider.valueOf(registrationId.toUpperCase()))
                                .enabled(true)
                                .build()
                ));

        UUID userId = appUser.getId();

        try {
            CreateUserProfileRequest profileRequest = CreateUserProfileRequest.newBuilder()
                    .setUserId(userId.toString())
                    .setUsername(username)
                    .setPhoneNumber(phoneNumber)
                    .setDateOfBirth(dateOfBirth)
                    .setGenderId(genderId)
                    .setAvatarUrl(avatarUrl)
                    .build();

            userProfileServiceGrpcClient.createUserProfile(profileRequest);
        } catch (Exception e) {
            log.error("Failed to create user profile in user-profile-service", e);
        }

        cookieUtils.addRefreshTokenCookie(
                response,
                jwtService.generateRefreshToken(email, userId.toString())
        );

        String targetUrl = "http://localhost:5173";
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
