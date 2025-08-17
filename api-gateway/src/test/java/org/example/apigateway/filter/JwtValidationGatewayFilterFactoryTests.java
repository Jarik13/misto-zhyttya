package org.example.apigateway.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpResponse;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.*;

class JwtValidationGatewayFilterFactoryTests {
    private WebClient.Builder webClientBuilder;
    private WebClient webClient;
    private JwtValidationGatewayFilterFactory filterFactory;
    private GatewayFilterChain chain;

    @BeforeEach
    void setUp() {
        webClientBuilder = mock(WebClient.Builder.class);
        webClient = mock(WebClient.class, RETURNS_DEEP_STUBS);

        when(webClientBuilder.baseUrl(any())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);

        filterFactory = new JwtValidationGatewayFilterFactory(webClientBuilder, "http://auth-service");

        chain = mock(GatewayFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());
    }

    @Test
    void givenNoAuthorizationHeader_whenFilterInvoked_thenUnauthorized() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filterFactory.apply(new Object()).filter(exchange, chain).block();

        MockServerHttpResponse response = exchange.getResponse();
        assert response.getStatusCode() == HttpStatus.UNAUTHORIZED;
        verify(chain, never()).filter(any());
    }

    @Test
    void givenEmptyAuthorizationHeader_whenFilterInvoked_thenUnauthorized() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/test")
                .header(HttpHeaders.AUTHORIZATION, "Bearer ")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filterFactory.apply(new Object()).filter(exchange, chain).block();

        assert exchange.getResponse().getStatusCode() == HttpStatus.UNAUTHORIZED;
        verify(chain, never()).filter(any());
    }
}
