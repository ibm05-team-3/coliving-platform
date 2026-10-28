package com.coliving.platform.security.filter;

import org.jspecify.annotations.NullMarked;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@NullMarked
public class UserIdHeaderWebFilter implements WebFilter {

    public static final String SUBJECT_HEADER = "X-User";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
                .flatMap(context -> Mono.justOrEmpty(context.getAuthentication()))
                .filter(this::authenticated)
                .map(authentication -> withUserId(exchange, authentication.getName()))
                .switchIfEmpty(Mono.fromSupplier(() -> withoutUserId(exchange)))
                .flatMap(chain::filter);
    }

    private boolean authenticated(Authentication authentication) {
        return authentication.isAuthenticated() && authentication.getName() != null && !authentication.getName().isBlank();
    }

    private ServerWebExchange withUserId(ServerWebExchange exchange, String userId) {
        HttpHeaders headers = HttpHeaders.copyOf(exchange.getRequest().getHeaders());
        headers.set(SUBJECT_HEADER, userId);
        return withHeaders(exchange, headers);
    }

    private ServerWebExchange withoutUserId(ServerWebExchange exchange) {
        if (!exchange.getRequest().getHeaders().containsHeader(SUBJECT_HEADER)) {
            return exchange;
        }
        HttpHeaders headers = HttpHeaders.copyOf(exchange.getRequest().getHeaders());
        headers.remove(SUBJECT_HEADER);
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