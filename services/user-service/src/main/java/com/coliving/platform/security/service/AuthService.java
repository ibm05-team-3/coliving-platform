package com.coliving.platform.security.service;

import com.coliving.platform.security.dao.RefreshTokenDAO;
import com.coliving.platform.security.dto.AuthTokenResponseDTO;
import com.coliving.platform.security.dto.RefreshRequestDTO;
import com.coliving.platform.security.jwt.AccessTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AccountStatusUserDetailsChecker;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AccessTokenProvider accessTokenProvider;
    private final RefreshTokenDAO refreshTokenDAO;
    private final CustomUserDetailsService userDetailsService;

    private final UserDetailsChecker userDetailsChecker = new AccountStatusUserDetailsChecker();

    public void removeRefresh(String rawRefreshToken) {
        refreshTokenDAO.deleteTokenHash(rawRefreshToken);
    }

    public AuthTokenResponseDTO refreshRotate(RefreshRequestDTO request) {
        String requestRawRefresh = request.refreshToken();

        Optional<String> optionalSub = refreshTokenDAO.findAndDelete(requestRawRefresh);
        if (optionalSub.isEmpty()) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        String accessSub = optionalSub.get();
        UserDetails userDetails = findAndValidateUser(accessSub);

        String newRawRefresh = refreshTokenDAO.generateAndSave(accessSub);
        String newAccessToken = accessTokenProvider.issueAccessToken(accessSub, userDetails.getAuthorities());
        return new AuthTokenResponseDTO(newAccessToken, newRawRefresh);
    }

    public AuthTokenResponseDTO issueTokens(String accessSub) {
        UserDetails userDetails = findAndValidateUser(accessSub);

        String newRawRefresh = refreshTokenDAO.generateAndSave(accessSub);
        String newAccessToken = accessTokenProvider.issueAccessToken(accessSub, userDetails.getAuthorities());
        return new AuthTokenResponseDTO(newAccessToken, newRawRefresh);
    }

    private UserDetails findAndValidateUser(String accessSub) {
        UserDetails userDetails = userDetailsService.loadUserById(accessSub);
        userDetailsChecker.check(userDetails);
        return userDetails;
    }
}
