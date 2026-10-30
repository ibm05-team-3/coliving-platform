package com.coliving.platform.security.oauth;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

public final class ReturnToAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    private final OAuth2AuthorizationRequestResolver delegate;

    public ReturnToAuthorizationRequestResolver(OAuth2AuthorizationRequestResolver delegate) {
        this.delegate = delegate;
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        return attach(delegate.resolve(request), request);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        return attach(delegate.resolve(request, clientRegistrationId), request);
    }

    private static OAuth2AuthorizationRequest attach(
            OAuth2AuthorizationRequest authorizationRequest,
            HttpServletRequest request
    ) {
        if (authorizationRequest == null) {
            return null;
        }
        String returnTo = SocialReturnPath.sanitize(request.getParameter(SocialReturnPath.ATTRIBUTE));
        if (returnTo == null) {
            return authorizationRequest;
        }
        // from() does not copy authorizationRequestUri, and build() would generate a new one.
        return OAuth2AuthorizationRequest.from(authorizationRequest)
                .authorizationRequestUri(authorizationRequest.getAuthorizationRequestUri())
                .attributes(attributes -> attributes.put(SocialReturnPath.ATTRIBUTE, returnTo))
                .build();
    }
}
