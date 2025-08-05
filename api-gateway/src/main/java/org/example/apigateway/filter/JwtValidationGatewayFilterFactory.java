package org.example.apigateway.filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;

import java.util.Optional;

@Component
public class JwtValidationGatewayFilterFactory extends AbstractGatewayFilterFactory<Object> {
    private final WebClient webClient;

    public JwtValidationGatewayFilterFactory(WebClient.Builder webClient,
                                             @Value("${auth.service.url}") String authServiceUrl) {
        this.webClient = webClient.baseUrl(authServiceUrl).build();
    }

    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> {
            String token = extractTokenFromCookie(exchange);

            if (token == null || token.isBlank()) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            return webClient.get()
                    .uri("/auth/validate")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .bodyToMono(String.class)
                    .flatMap(userId -> {
                        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                                .header("X-User-Id", userId)
                                .build();

                        return chain.filter(exchange.mutate().request(mutatedRequest).build());
                    })
                    .onErrorResume(ex -> {
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        return exchange.getResponse().setComplete();
                    });
        };
    }

    private String extractTokenFromCookie(ServerWebExchange exchange) {
        return Optional.ofNullable(exchange.getRequest().getCookies().getFirst("access_token"))
                .map(HttpCookie::getValue)
                .orElseThrow(() -> new RuntimeException("Access token cookie is missing"));
    }
}
