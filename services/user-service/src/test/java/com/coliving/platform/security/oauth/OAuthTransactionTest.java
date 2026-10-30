package com.coliving.platform.security.oauth;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.endpoint.PkceParameterNames;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OAuthTransactionTest {

    @Test
    void keepsProviderAgnosticAuthorizationRequest() {
        OAuth2AuthorizationRequest request = OAuth2AuthorizationRequest.authorizationCode()
                .authorizationUri("https://nid.naver.com/oauth2.0/authorize")
                .clientId("naver-client")
                .redirectUri("http://localhost:8081/user-service/user/oauth2/code/naver")
                .scopes(Set.of("name", "email", "mobile"))
                .state("state-1")
                .additionalParameters(Map.of(PkceParameterNames.CODE_CHALLENGE, "challenge"))
                .attributes(Map.of(
                        OAuth2ParameterNames.REGISTRATION_ID, "naver",
                        PkceParameterNames.CODE_VERIFIER, "verifier",
                        SocialReturnPath.ATTRIBUTE, "/community?tab=1"
                ))
                .authorizationRequestUri("https://nid.naver.com/oauth2.0/authorize?state=state-1")
                .build();

        OAuth2AuthorizationRequest restored = OAuthTransaction.from(request).toRequest();

        assertEquals("naver", restored.getAttribute(OAuth2ParameterNames.REGISTRATION_ID));
        assertEquals("verifier", restored.getAttribute(PkceParameterNames.CODE_VERIFIER));
        assertEquals("/community?tab=1", restored.getAttribute(SocialReturnPath.ATTRIBUTE));
        assertEquals("challenge", restored.getAdditionalParameters().get(PkceParameterNames.CODE_CHALLENGE));
        assertEquals(request.getAuthorizationRequestUri(), restored.getAuthorizationRequestUri());
        assertEquals(request.getState(), restored.getState());
    }
}
