package com.example.authsystem.config;

import com.example.authsystem.service.LocalUserDetailsService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableConfigurationProperties(AppProperties.class)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   AuthenticationSuccessHandler authenticationSuccessHandler) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(Customizer.withDefaults())
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/error", "/api/auth/login", "/api/auth/logout", "/api/auth/me", "/api/sso/login", "/api/sso/validate", "/api/sso/logout").permitAll()
                .requestMatchers(HttpMethod.GET, "/oauth2/authorization/**", "/login/oauth2/code/**").permitAll()
                .anyRequest().authenticated())
            .oauth2Login(oauth2 -> oauth2
                .successHandler(authenticationSuccessHandler)
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(oAuth2UserService())
                    .oidcUserService(oidcUserService())))
            .logout(logout -> logout.logoutSuccessHandler((request, response, authentication) -> response.setStatus(HttpServletResponse.SC_OK)));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public LocalUserDetailsService localUserDetailsService(AppProperties appProperties, PasswordEncoder passwordEncoder) {
        return new LocalUserDetailsService(appProperties, passwordEncoder);
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler(AppProperties appProperties) {
        return new FrontendRedirectAuthenticationSuccessHandler(appProperties);
    }

    @Bean
    public OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService() {
        DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
        return request -> {
            OAuth2User user = delegate.loadUser(request);
            List<GrantedAuthority> authorities = user.getAuthorities().stream()
                .collect(Collectors.toList());
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
            String nameAttributeKey = request.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();
            if (nameAttributeKey == null || nameAttributeKey.isBlank()) {
                nameAttributeKey = "name";
            }
            return new DefaultOAuth2User(authorities, user.getAttributes(), nameAttributeKey);
        };
    }

    @Bean
    public OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService() {
        OidcUserService delegate = new OidcUserService();
        return request -> {
            OidcUser user = delegate.loadUser(request);
            List<GrantedAuthority> authorities = user.getAuthorities().stream()
                .collect(Collectors.toList());
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
            return new DefaultOidcUser(authorities, user.getIdToken(), user.getUserInfo());
        };
    }

    static class FrontendRedirectAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

        private final AppProperties appProperties;

        FrontendRedirectAuthenticationSuccessHandler(AppProperties appProperties) {
            this.appProperties = appProperties;
        }

        @Override
        public void onAuthenticationSuccess(HttpServletRequest request,
                                            HttpServletResponse response,
                                            Authentication authentication) throws IOException, ServletException {
            String redirect = request.getParameter("redirect_uri");
            if (redirect == null || redirect.isBlank()) {
                redirect = appProperties.getFrontendBaseUrl() + appProperties.getDefaultRedirectUri();
            }
            response.sendRedirect(redirect);
        }
    }
}
