package org.example.authservice.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class KeyUtilsTests {
    private KeyUtils keyUtils;
    private SecretsManagerClient secretsManagerClient;

    @BeforeEach
    void setUp() {
        secretsManagerClient = mock(SecretsManagerClient.class);
        keyUtils = new KeyUtils(secretsManagerClient);
    }

    @Test
    void givenSecretsManagerThrows_whenLoadKeys_thenExceptionIsThrown() {
        // given
        when(secretsManagerClient.getSecretValue(any(GetSecretValueRequest.class)))
                .thenThrow(new RuntimeException("AWS error"));

        // when / then
        RuntimeException ex = assertThrows(RuntimeException.class, keyUtils::loadKeys);
        assertTrue(ex.getMessage().contains("Failed to load keys"));
    }
}
