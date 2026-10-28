package com.coliving.platform.security.filter;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

public class SessionCookieBearerWebFilter implements WebFilter {

    private static final ParameterizedTypeReference<Map<String, String>> ACCESS_TOKEN_BODY =
            new ParameterizedTypeReference<>() {};
    private static final Duration RESOLVE_TIMEOUT = Duration.ofSeconds(2);

    private final WebClient webClient;

    public SessionCookieBearerWebFilter(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        if (hasBearer(exchange) || isAnonymousAuthRequest(exchange)) {
            return chain.filter(exchange);
        }
        String cookie = exchange.getRequest().getHeaders().getFirst(HttpHeaders.COOKIE);
        if (cookie == null || cookie.isBlank()) {
            return chain.filter(exchange);
        }
        return resolveAccessToken(cookie)
                .flatMap(token -> chain.filter(withBearer(exchange, token)))
                .switchIfEmpty(Mono.defer(() -> chain.filter(exchange)));
    }

    private Mono<String> resolveAccessToken(String cookie) {
        return webClient.get()
                .uri("/internal/session/access")
                .header(HttpHeaders.COOKIE, cookie)
                .exchangeToMono(response -> readToken(response.statusCode(), response))
                .timeout(RESOLVE_TIMEOUT)
                .onErrorResume(ex -> Mono.empty());
    }

    private Mono<String> readToken(HttpStatusCode status, ClientResponse response) {
        if (!status.is2xxSuccessful()) {
            return response.releaseBody().then(Mono.empty());
        }
        return response.bodyToMono(ACCESS_TOKEN_BODY)
                .flatMap(body -> {
                    String token = body.get("accessToken");
                    if (token == null || token.isBlank()) {
                        return Mono.empty();
                    }
                    return Mono.just(token);
                });
    }

    private boolean hasBearer(ServerWebExchange exchange) {
        String header = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        return header != null && header.startsWith("Bearer ") && header.length() > "Bearer ".length();
    }

    private boolean isAnonymousAuthRequest(ServerWebExchange exchange) {
        String path = exchange.getRequest().getURI().getPath();
        return path.endsWith("/user/login")
                || path.contains("/oauth2/authorization/")
                || path.contains("/oauth2/code/");
    }

    private ServerWebExchange withBearer(ServerWebExchange exchange, String accessToken) {
        HttpHeaders headers = HttpHeaders.copyOf(exchange.getRequest().getHeaders());
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        return withHeaders(exchange, headers);
    }

    private ServerWebExchange withHeaders(ServerWebExchange exchange, HttpHeaders headers) {
        ServerHttpRequest request = new ServerHttpRequestDecorator(exchange.getRequest()) {
            @Override
            public HttpHeaders getHeaders() {
                return headers;
            }
        };
        return exchange.mutate().request(request).build();
    }
}
