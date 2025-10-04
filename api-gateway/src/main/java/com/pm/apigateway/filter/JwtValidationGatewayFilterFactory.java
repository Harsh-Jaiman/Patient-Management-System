package com.pm.apigateway.filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Gateway filter for validating JWT Access Tokens
 * This filter checks requests for Authorization header,
 * calls AuthService /validate/access endpoint, and blocks unauthorized requests.
 * Refresh tokens are not validated here; they should be used directly by clients.
 */
@Component
public class JwtValidationGatewayFilterFactory extends AbstractGatewayFilterFactory<Object> {

    private final WebClient webClient;

    // Inject WebClient and AuthService URL
    public JwtValidationGatewayFilterFactory(WebClient.Builder webClientBuilder,
                                             @Value("${auth.service.url}") String authServiceUrl
    ) {
        this.webClient = webClientBuilder.baseUrl(authServiceUrl).build();
    }

    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> {

            // 1️⃣ Extract Authorization header
            String token = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            // 2️⃣ If missing or not in "Bearer <token>" format → reject
            if (token == null || !token.startsWith("Bearer ")) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            // 3️⃣ Call AuthService to validate access token
            //    Note: We are calling `/validate/access` endpoint
            return webClient.get()
                    .uri("/validate/access") // only access token validation
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .retrieve()
                    .toBodilessEntity()
                    .flatMap(response -> {
                        // 4️⃣ If AuthService returns 200 → continue request
                        if (response.getStatusCode().is2xxSuccessful()) {
                            return chain.filter(exchange);
                        } else {
                            // 5️⃣ If token invalid → return 401
                            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                            return exchange.getResponse().setComplete();
                        }
                    })
                    // 6️⃣ Handle exceptions (network/server errors)
                    .onErrorResume(ex -> {
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        return exchange.getResponse().setComplete();
                    });
        };
    }
}
