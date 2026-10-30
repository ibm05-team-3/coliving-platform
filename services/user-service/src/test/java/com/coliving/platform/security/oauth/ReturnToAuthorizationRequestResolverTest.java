package com.coliving.platform.security.oauth;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.endpoint.PkceParameterNames;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class ReturnToAuthorizationRequestResolverTest {

    private static final String AUTHORIZATION_URI =
            "https://accounts.google.com/o/oauth2/v2/auth?state=state-1&code_challenge=challenge";

    @Test
    void storesReturnPathWithoutChangingTheProviderRequest() {
        OAuth2AuthorizationRequest original = authorizationRequest();
        ReturnToAuthorizationRequestResolver resolver =
                new ReturnToAuthorizationRequestResolver(delegate(original));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter(SocialReturnPath.ATTRIBUTE, "/community?tab=1");

        OAuth2AuthorizationRequest resolved = resolver.resolve(request);

        assertEquals("/community?tab=1", resolved.getAttribute(SocialReturnPath.ATTRIBUTE));
        assertEquals("google", resolved.getAttribute(OAuth2ParameterNames.REGISTRATION_ID));
        assertEquals("verifier", resolved.getAttribute(PkceParameterNames.CODE_VERIFIER));
        assertEquals("challenge", resolved.getAdditionalParameters().get(PkceParameterNames.CODE_CHALLENGE));
        assertEquals(AUTHORIZATION_URI, resolved.getAuthorizationRequestUri());
        assertNull(resolved.getAdditionalParameters().get(SocialReturnPath.ATTRIBUTE));
    }

    @Test
    void dropsUnsafeReturnPath() {
        OAuth2AuthorizationRequest original = authorizationRequest();
        ReturnToAuthorizationRequestResolver resolver =
                new ReturnToAuthorizationRequestResolver(delegate(original));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter(SocialReturnPath.ATTRIBUTE, "//evil.example");

        assertSame(original, resolver.resolve(request));
    }

    private static OAuth2AuthorizationRequest authorizationRequest() {
        return OAuth2AuthorizationRequest.authorizationCode()
                .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                .clientId("google-client")
                .redirectUri("http://localhost:8081/user-service/user/oauth2/code/google")
                .state("state-1")
                .additionalParameters(Map.of(PkceParameterNames.CODE_CHALLENGE, "challenge"))
                .attributes(Map.of(
                        OAuth2ParameterNames.REGISTRATION_ID, "google",
                        PkceParameterNames.CODE_VERIFIER, "verifier"
                ))
                .authorizationRequestUri(AUTHORIZATION_URI)
                .build();
    }

    private static OAuth2AuthorizationRequestResolver delegate(OAuth2AuthorizationRequest authorizationRequest) {
        return new OAuth2AuthorizationRequestResolver() {
            @Override
            public OAuth2AuthorizationRequest resolve(jakarta.servlet.http.HttpServletRequest request) {
                return authorizationRequest;
            }

            @Override
            public OAuth2AuthorizationRequest resolve(
                    jakarta.servlet.http.HttpServletRequest request,
                    String clientRegistrationId
            ) {
                return authorizationRequest;
            }
        };
    }
}
