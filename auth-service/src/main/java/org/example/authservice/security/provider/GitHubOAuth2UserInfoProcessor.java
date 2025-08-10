package org.example.authservice.security.provider;

import lombok.extern.slf4j.Slf4j;
import org.example.authservice.dto.gender.Gender;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Component("github")
public class GitHubOAuth2UserInfoProcessor implements OAuth2UserInfoProcessor {
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public Map<String, Object> enrichAttributes(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());

        String token = userRequest.getAccessToken().getTokenValue();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                "https://api.github.com/user/emails",
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<>() {}
        );

        if (response.getStatusCode().is2xxSuccessful()) {
            List<Map<String, Object>> emailList = response.getBody();
            if (emailList != null) {
                for (Map<String, Object> emailObj : emailList) {
                    boolean primary = Boolean.TRUE.equals(emailObj.get("primary"));
                    boolean verified = Boolean.TRUE.equals(emailObj.get("verified"));
                    if (primary && verified) {
                        attributes.put("email", emailObj.get("email"));
                        break;
                    }
                }
            }
        }

        return attributes;
    }

    @Override
    public String getNameAttributeKey() {
        return "login";
    }

    @Override
    public Map<String, Object> extractAttributes(OAuth2User oAuth2User) {
        Map<String, Object> attrs = new HashMap<>();
        Map<String, Object> orig = oAuth2User.getAttributes();

        attrs.put("username", orig.getOrDefault("login", ""));
        attrs.put("phoneNumber", "");
        attrs.put("dateOfBirth", "");
        attrs.put("genderId", Gender.OTHER.getId());
        attrs.put("avatarUrl", orig.getOrDefault("avatar_url", ""));

        return attrs;
    }
}
