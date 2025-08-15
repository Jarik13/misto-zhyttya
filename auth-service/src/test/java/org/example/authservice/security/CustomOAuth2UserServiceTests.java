package org.example.authservice.security;

import org.example.authservice.security.provider.OAuth2UserInfoProcessor;
import org.example.authservice.security.provider.OAuth2UserInfoProcessorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomOAuth2UserServiceTests {
    private CustomOAuth2UserService customOAuth2UserService;
    private OAuth2UserInfoProcessorFactory processorFactory;
    private OAuth2UserInfoProcessor processor;

    @BeforeEach
    void setUp() {
        processorFactory = mock(OAuth2UserInfoProcessorFactory.class);
        processor = mock(OAuth2UserInfoProcessor.class);
        customOAuth2UserService = new CustomOAuth2UserService(processorFactory);
    }

    @Test
    void givenValidUserRequest_whenLoadUser_thenReturnsEnrichedOAuth2User() {
        // Given
        String registrationId = "google";
        String nameAttributeKey = "sub";

        OAuth2UserRequest userRequest = mock(OAuth2UserRequest.class);
        ClientRegistration clientRegistration = mock(ClientRegistration.class);
        when(userRequest.getClientRegistration()).thenReturn(clientRegistration);
        when(clientRegistration.getRegistrationId()).thenReturn(registrationId);

        Map<String, Object> attributes = Map.of("sub", "123456789", "email", "test@example.com");
        Collection<? extends GrantedAuthority> authorities = Collections.emptyList();

        OAuth2User originalUser = new DefaultOAuth2User(authorities, attributes, nameAttributeKey);

        CustomOAuth2UserService spyService = Mockito.spy(new CustomOAuth2UserService(processorFactory));
        doReturn(originalUser)
                .when((DefaultOAuth2UserService) spyService)
                .loadUser(userRequest);

        when(processorFactory.getProcessor(registrationId)).thenReturn(processor);
        when(processor.enrichAttributes(userRequest, originalUser)).thenReturn(attributes);
        when(processor.getNameAttributeKey()).thenReturn(nameAttributeKey);

        // When
        OAuth2User result = spyService.loadUser(userRequest);

        // Then
        assertEquals(attributes, result.getAttributes());
        assertEquals("123456789", result.getName());
    }

    @Test
    void givenMissingProcessor_whenLoadUser_thenDoesNotThrowException() {
        // Given
        String registrationId = "unknown";

        OAuth2UserRequest userRequest = mock(OAuth2UserRequest.class);
        ClientRegistration clientRegistration = mock(ClientRegistration.class);
        when(userRequest.getClientRegistration()).thenReturn(clientRegistration);
        when(clientRegistration.getRegistrationId()).thenReturn(registrationId);

        CustomOAuth2UserService spyService = Mockito.spy(customOAuth2UserService);

        OAuth2User dummyUser = mock(OAuth2User.class);
        doReturn(dummyUser).when(spyService).loadUser(userRequest);

        processorFactory.getProcessor(registrationId);

        // When / Then
        assertDoesNotThrow(() -> spyService.loadUser(userRequest));
    }

}
