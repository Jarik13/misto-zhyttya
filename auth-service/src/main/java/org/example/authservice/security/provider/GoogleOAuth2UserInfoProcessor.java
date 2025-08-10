package org.example.authservice.security.provider;

import org.example.authservice.dto.gender.Gender;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component("google")
public class GoogleOAuth2UserInfoProcessor implements OAuth2UserInfoProcessor {
    @Override
    public Map<String, Object> enrichAttributes(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
        return new HashMap<>(oAuth2User.getAttributes());
    }

    @Override
    public String getNameAttributeKey() {
        return "sub";
    }

    @Override
    public Map<String, Object> extractAttributes(OAuth2User oAuth2User) {
        Map<String, Object> attrs = new HashMap<>();

        Map<String, Object> orig = oAuth2User.getAttributes();

        attrs.put("username", orig.getOrDefault("name", ""));
        attrs.put("phoneNumber", "");
        attrs.put("dateOfBirth", "");
        attrs.put("genderId", Gender.OTHER.getId());
        attrs.put("avatarKey", orig.getOrDefault("picture", ""));

        return attrs;
    }
}