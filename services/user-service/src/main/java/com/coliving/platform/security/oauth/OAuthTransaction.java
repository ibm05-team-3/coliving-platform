package com.coliving.platform.security.oauth;

import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public record OAuthTransaction(
        String authorizationUri,
        String clientId,
        String redirectUri,
        Set<String> scopes,
        String state,
        String authorizationRequestUri,
        Map<String, String> additionalParameters,
        Map<String, String> attributes
) {
    public OAuthTransaction {
        scopes = scopes == null ? Set.of() : Set.copyOf(scopes);
        additionalParameters = additionalParameters == null ? Map.of() : Map.copyOf(additionalParameters);
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }

    public static OAuthTransaction from(OAuth2AuthorizationRequest request) {
        return new OAuthTransaction(
                request.getAuthorizationUri(),
                request.getClientId(),
                request.getRedirectUri(),
                request.getScopes(),
                request.getState(),
                request.getAuthorizationRequestUri(),
                textValues(request.getAdditionalParameters()),
                textValues(request.getAttributes())
        );
    }

    public OAuth2AuthorizationRequest toRequest() {
        return OAuth2AuthorizationRequest.authorizationCode()
                .authorizationUri(authorizationUri)
                .clientId(clientId)
                .redirectUri(redirectUri)
                .scopes(scopes)
                .state(state)
                .additionalParameters(new LinkedHashMap<>(additionalParameters))
                .attributes(new LinkedHashMap<>(attributes))
                .authorizationRequestUri(authorizationRequestUri)
                .build();
    }

    private static Map<String, String> textValues(Map<String, Object> source) {
        Map<String, String> values = new LinkedHashMap<>();
        source.forEach((key, value) -> {
            if (!(value instanceof String text)) {
                throw new IllegalStateException("oauth parameter must be text: " + key);
            }
            values.put(key, text);
        });
        return values;
    }
}
