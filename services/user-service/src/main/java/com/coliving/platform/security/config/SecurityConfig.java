package com.coliving.platform.security.config;

import com.coliving.platform.security.filter.LoginFilter;
import com.coliving.platform.security.handler.LoginSuccessHandler;
import com.coliving.platform.security.http.SessionCookieFactory;
import com.coliving.platform.security.oauth.RedisOAuth2AuthorizationRequestRepository;
import com.coliving.platform.security.service.AuthService;
import com.coliving.platform.security.service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestCustomizers;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private static final String LOGIN_URL = "/user/login";
    private static final String OAUTH_AUTHORIZATION_BASE_URI = "/user/oauth2/authorization";
    private static final String OAUTH_CALLBACK_URI = "/user/oauth2/code/*";

    private final AuthenticationSuccessHandler socialSuccessHandler;
    private final AuthenticationFailureHandler socialFailureHandler;

    private final CustomOAuth2UserService customOAuth2UserService;
    private final AuthService authService;
    private final SessionCookieFactory sessionCookieFactory;
    private final RedisOAuth2AuthorizationRequestRepository authorizationRequestRepository;

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    OAuth2AuthorizationRequestResolver authorizationRequestResolver(ClientRegistrationRepository registrations) {
        DefaultOAuth2AuthorizationRequestResolver resolver =
                new DefaultOAuth2AuthorizationRequestResolver(registrations, OAUTH_AUTHORIZATION_BASE_URI);
        resolver.setAuthorizationRequestCustomizer(OAuth2AuthorizationRequestCustomizers.withPkce());
        return resolver;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           AuthenticationManager authenticationManager,
                                           OAuth2AuthorizationRequestResolver authorizationRequestResolver) throws Exception {

        // 로그인 필터
        LoginFilter loginFilter = new LoginFilter(authenticationManager, LOGIN_URL);
        loginFilter.setAuthenticationSuccessHandler(new LoginSuccessHandler(authService, sessionCookieFactory));

        // http 설정
        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                )
                .oauth2Login(oauth -> oauth
                        .authorizationEndpoint(endpoint -> endpoint
                                .authorizationRequestResolver(authorizationRequestResolver)
                                .authorizationRequestRepository(authorizationRequestRepository)
                        )
                        .redirectionEndpoint(endpoint -> endpoint.baseUri(OAUTH_CALLBACK_URI))
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .successHandler(socialSuccessHandler)
                        .failureHandler(socialFailureHandler)
                )
                .addFilterAt(loginFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}