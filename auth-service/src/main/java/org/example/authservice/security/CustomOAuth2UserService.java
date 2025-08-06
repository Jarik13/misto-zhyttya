package org.example.authservice.security;

import lombok.RequiredArgsConstructor;
import org.example.authservice.security.provider.OAuth2UserInfoProcessorFactory;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final OAuth2UserInfoProcessorFactory processorFactory;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User user = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        var processor = processorFactory.getProcessor(registrationId);
        Map<String, Object> enrichedAttributes = processor.enrichAttributes(userRequest, user);

        return new DefaultOAuth2User(
                user.getAuthorities(),
                enrichedAttributes,
                processor.getNameAttributeKey()
        );
    }
}
