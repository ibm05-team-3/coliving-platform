package com.coliving.platform.security.service;

import com.coliving.platform.security.config.AuthProperties;
import com.coliving.platform.security.dao.BrowserSessionDAO;
import com.coliving.platform.security.dao.RefreshTokenDAO;
import com.coliving.platform.security.jwt.AccessTokenProvider;
import com.coliving.platform.security.session.BrowserSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AccountStatusUserDetailsChecker;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private static final Duration ACCESS_REFRESH_SKEW = Duration.ofSeconds(15);

    private final AccessTokenProvider accessTokenProvider;
    private final RefreshTokenDAO refreshTokenDAO;
    private final BrowserSessionDAO browserSessionDAO;
    private final CustomUserDetailsService userDetailsService;
    private final AuthProperties authProperties;

    private final UserDetailsChecker userDetailsChecker = new AccountStatusUserDetailsChecker();

    public String openSession(String accessSub) {
        return browserSessionDAO.saveNew(issue(accessSub));
    }

    public Optional<String> sessionSubject(String sessionId) {
        return browserSessionDAO.find(sessionId).map(BrowserSession::sub);
    }

    public Optional<String> currentAccessToken(String sessionId) {
        Optional<BrowserSession> stored = browserSessionDAO.find(sessionId);
        if (stored.isEmpty()) {
            return Optional.empty();
        }
        BrowserSession session = stored.get();
        if (accessStillValid(session)) {
            return Optional.of(session.accessToken());
        }

        Optional<String> rotatedSub = refreshTokenDAO.findAndDelete(session.refreshToken());
        if (rotatedSub.isEmpty()) {
            return browserSessionDAO.find(sessionId)
                    .filter(latest -> latest.accessExpiresAtEpochMilli() > Instant.now().toEpochMilli())
                    .map(BrowserSession::accessToken);
        }
        if (!rotatedSub.get().equals(session.sub())) {
            browserSessionDAO.delete(sessionId);
            return Optional.empty();
        }

        BrowserSession renewed = issue(rotatedSub.get());
        browserSessionDAO.save(sessionId, renewed);
        return Optional.of(renewed.accessToken());
    }

    public void closeSession(String sessionId) {
        browserSessionDAO.find(sessionId)
                .ifPresent(session -> refreshTokenDAO.deleteTokenHash(session.refreshToken()));
        browserSessionDAO.delete(sessionId);
    }

    private BrowserSession issue(String accessSub) {
        UserDetails userDetails = findAndValidateUser(accessSub);
        Instant accessExpiresAt = Instant.now().plus(authProperties.accessTokenExpiration());
        String refreshToken = refreshTokenDAO.generateAndSave(accessSub);
        String accessToken = accessTokenProvider.issueAccessToken(accessSub, userDetails.getAuthorities());
        return new BrowserSession(accessSub, refreshToken, accessToken, accessExpiresAt.toEpochMilli());
    }

    private boolean accessStillValid(BrowserSession session) {
        return session.accessExpiresAtEpochMilli() - ACCESS_REFRESH_SKEW.toMillis() > Instant.now().toEpochMilli();
    }

    private UserDetails findAndValidateUser(String accessSub) {
        UserDetails userDetails = userDetailsService.loadUserById(accessSub);
        userDetailsChecker.check(userDetails);
        return userDetails;
    }
}
