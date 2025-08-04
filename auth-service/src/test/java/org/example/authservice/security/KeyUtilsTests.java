package org.example.authservice.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class KeyUtilsTests {
    private KeyUtils keyUtils;
    private SecretsManagerClient secretsManagerClient;

    private static final String MOCK_PRIVATE_KEY = """
            -----BEGIN PRIVATE KEY-----
            MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCqFvgiSzQLKLi4
            6AkhtyjAalSKr13d/HkqisXHGpoukKHjDAk3crIrt0fMFC65wEaNIjarInOpkjGd
            myB2MepOiUmzoaWaEtfHEjyjIwOd0P9+Ds6hWcrQ13tCcsNEBnE2x82StnCz/CMO
            W1iTd6gCOops5xg0ilDL7OgJIqLPIGtMHuU55XB+80vG1mB50Fj7op6T95zW6k/b
            9Chdmf2spsjhU0M0qxQvbtReBYoGh7iUj4KdGcdbYUDyX3w91vw07Idj+gFpmQey
            8fu3evFF3gRK4h4cvwmI6nrPSQ719coJENUf9wbq+GS9EdQLJqwxgWUel6hByPyJ
            NATDjS4NAgMBAAECggEACw6wNeaGrR0KGTc4dMEn/I5NDbEF3KsPo3TfE6kGkfVk
            GNMM/FZP/+iL2dgMC5ZU39L0E98K/uCaqiDOFPD8xvtE19Ev51rHPVFRsNdTYlyW
            Z3cF9tZUCYygnjrAt/jlPkbx9dI3dHP1QXctxHR4byCoJBlH6Q5+3yr5ELL4O5fI
            Nugc3G6yyxbJAyTIAqhWa4fmUAQX3TGipGVcCUxlVY6oF9VvImd+Xzr/ZwcFK7MH
            4MAVTt4H+qj8WXt+NmYoeDhPyBU+N6OLN/2Ed6PNCl6sJTvKlW/MeTZqncc2Tx4n
            2LxlnSLQ1pSJelLhvPrserhel7OAHXKhb7+4+oYb7wKBgQDjTTgaj3MrdTV73z79
            oz22kWyGwT7jqCRNqYDk45uXX/52/HOW3eAqxM6wqVxOxUv4W9mSREfr+UzPd4/9
            SHXLHwlcHpPk5BoW9DFmbc43gCtsYhoDdaDdeE6RCqnfYkHBgFu7TCK223HnyL6+
            ZI1Q2uPVF5P969a2PDq5GA0lGwKBgQC/kI/66a8ndUMb1CK33V8qVtgYusCpe5gz
            xE+bKe6ebGJHlvgwx7fY/9Q6DncqztDhu6wcdCON47/XJgmWlEHN47KtyTpZowr7
            Om1GeXUqH9hbaFHdOvf2WLhI0FGrJ+EpIzx0neAmwdcuZjKS6Pw7bG+jMNv1gepK
            Plz3Sw4z9wKBgHYzEZm+PwW6TbW8weTjnpPxy53HoAyCw14fZwef1KlDIfB+Fx29
            A3U6IsF7UKUls/vSx7FoEH6FhYvdgOMVayvVV+ivffjpJSgKlCXPtAzoeaxC14Q1
            BxIkHbcO9IdVcRxOw2UCkorEUtdVbNtVLqd9LD0DpdHFckIRz/ewqiVBAoGAHPwG
            jPRmqBW5/a/vQNqh14okUDmRhRNKSCrf0J5bmGTVyfcxvk+5pX+v4Mjnhvab7Eu1
            EukPqBxZnngG412zRy+4jUrxJTbiJnkEp7Eg5SrEVbBHEq4lboSl/X5nnPn/u3W1
            Y/+vNq4FnOOhq1DNOUCC87Ik1dS+WlVtQ417rKkCgYEAqreT8k8Vpm2nu43o4OwG
            x7smWJ+iO9Nd17/xW8FQ6cBieQLJkfX24WmWckkvTa6xC8o9w+Yso3hyKST4rIUf
            CIBhuWnin6bx6LXSYO1PpCpD7wfIfzZAJX4Ig88x7VkHOyqvUGCVI/XQvvXBHuCO
            o/B1T/aSu6ZdgAiJM0+1nXw=
            -----END PRIVATE KEY-----
            """;

    private static final String MOCK_PUBLIC_KEY = """
            -----BEGIN PUBLIC KEY-----
            MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAqhb4Iks0Cyi4uOgJIbco
            wGpUiq9d3fx5KorFxxqaLpCh4wwJN3KyK7dHzBQuucBGjSI2qyJzqZIxnZsgdjHq
            TolJs6GlmhLXxxI8oyMDndD/fg7OoVnK0Nd7QnLDRAZxNsfNkrZws/wjDltYk3eo
            AjqKbOcYNIpQy+zoCSKizyBrTB7lOeVwfvNLxtZgedBY+6Kek/ec1upP2/QoXZn9
            rKbI4VNDNKsUL27UXgWKBoe4lI+CnRnHW2FA8l98Pdb8NOyHY/oBaZkHsvH7t3rx
            Rd4ESuIeHL8JiOp6z0kO9fXKCRDVH/cG6vhkvRHUCyasMYFlHpeoQcj8iTQEw40u
            DQIDAQAB
            -----END PUBLIC KEY-----
            """;

    @BeforeEach
    void setUp() {
        secretsManagerClient = mock(SecretsManagerClient.class);
        keyUtils = new KeyUtils(secretsManagerClient);
    }

    @Test
    void givenValidSecrets_whenLoadKeys_thenKeysAreParsedCorrectly() throws Exception {
        // given
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> secretsMap = Map.of(
                "privateKey", MOCK_PRIVATE_KEY,
                "publicKey", MOCK_PUBLIC_KEY
        );
        String secretJson = mapper.writeValueAsString(secretsMap);

        GetSecretValueResponse mockResponse = mock(GetSecretValueResponse.class);
        when(mockResponse.secretString()).thenReturn(secretJson);
        when(secretsManagerClient.getSecretValue(any(GetSecretValueRequest.class)))
                .thenReturn(mockResponse);

        // when
        keyUtils.loadKeys();

        // then
        PrivateKey privateKey = keyUtils.getPrivateKey();
        PublicKey publicKey = keyUtils.getPublicKey();

        assertNotNull(privateKey);
        assertNotNull(publicKey);
        assertEquals("RSA", privateKey.getAlgorithm());
        assertEquals("RSA", publicKey.getAlgorithm());
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
