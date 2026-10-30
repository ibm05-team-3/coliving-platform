package com.coliving.platform.security.oauth;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SocialReturnPathTest {

    @Test
    void keepsInAppPathAndQuery() {
        assertEquals("/community", SocialReturnPath.sanitize("/community"));
        assertEquals("/community?tab=1&q=a", SocialReturnPath.sanitize("/community?tab=1&q=a"));
    }

    @Test
    void rejectsValuesThatWouldLeaveTheApp() {
        assertNull(SocialReturnPath.sanitize(null));
        assertNull(SocialReturnPath.sanitize(""));
        assertNull(SocialReturnPath.sanitize("https://evil.example"));
        assertNull(SocialReturnPath.sanitize("//evil.example"));
        assertNull(SocialReturnPath.sanitize("/\\evil.example"));
        assertNull(SocialReturnPath.sanitize("/login"));
        assertNull(SocialReturnPath.sanitize("/login?from=/community"));
        assertNull(SocialReturnPath.sanitize("/community\r\nLocation: https://evil.example"));
        assertNull(SocialReturnPath.sanitize("/https://evil.example"));
    }

    @Test
    void redirectUsesFrontendOriginPlusPath() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        OAuth2AuthorizationRequest authorizationRequest = OAuth2AuthorizationRequest.authorizationCode()
                .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                .clientId("google-client")
                .redirectUri("http://localhost:8081/user-service/user/oauth2/code/google")
                .state("state-1")
                .attributes(Map.of(SocialReturnPath.ATTRIBUTE, "/community?tab=1"))
                .build();

        SocialReturnPath.remember(request, authorizationRequest);

        assertEquals(
                "http://localhost:5173/community?tab=1",
                SocialReturnPath.redirect("http://localhost:5173", request)
        );
        assertEquals(
                "http://localhost:5173/community?tab=1",
                SocialReturnPath.redirect("http://localhost:5173/", request)
        );
    }

    @Test
    void redirectStaysOnFrontendWhenReturnPathIsMissing() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        assertEquals("http://localhost:5173", SocialReturnPath.redirect("http://localhost:5173", request));
    }
}
