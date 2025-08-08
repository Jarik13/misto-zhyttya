package org.example.authservice.security.provider;

import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Map;

public interface OAuth2UserInfoProcessor {
    Map<String, Object> enrichAttributes(OAuth2UserRequest userRequest, OAuth2User oAuth2User);
    String getNameAttributeKey();
    Map<String, Object> extractAttributes(OAuth2User oAuth2User);
}
