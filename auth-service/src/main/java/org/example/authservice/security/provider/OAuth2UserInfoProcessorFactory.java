package org.example.authservice.security.provider;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class OAuth2UserInfoProcessorFactory {
    private final Map<String, OAuth2UserInfoProcessor> processors;

    public OAuth2UserInfoProcessor getProcessor(String registrationId) {
        return processors.getOrDefault(registrationId.toLowerCase(), processors.get("default"));
    }
}
