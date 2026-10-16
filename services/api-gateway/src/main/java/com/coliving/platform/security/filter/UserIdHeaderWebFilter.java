package com.coliving.platform.security.filter;

import org.jspecify.annotations.NullMarked;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@NullMarked
public class UserIdHeaderWebFilter implements WebFilter {

    public static final String SUBJECT_HEADER = "X-User-Id";

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
        return exchange.mutate()
                .request(builder -> builder.headers(headers -> {
                    headers.remove(SUBJECT_HEADER);
                    headers.set(SUBJECT_HEADER, userId);
                }))
                .build();
    }

    private ServerWebExchange withoutUserId(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest().mutate()
                .headers(headers -> headers.remove(SUBJECT_HEADER))
                .build();
        return exchange.mutate().request(request).build();
    }
}