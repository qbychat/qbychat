package org.qbynet.authorization.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import jakarta.annotation.Resource;
import org.qbynet.authorization.federation.FederatedIdentityIdTokenCustomizer;
import org.qbynet.authorization.jose.Jwks;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;


@Configuration(proxyBeanMethods = false)
public class AuthorizationServerConfig {
    private static final String CUSTOM_CONSENT_PAGE_URI = "/oauth2/consent";

    @Resource
    Jwks jwks;

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
                new OAuth2AuthorizationServerConfigurer();
        http
                .securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
                .with(authorizationServerConfigurer, conf -> conf
                        .authorizationEndpoint(authorizationEndpoint ->
                                authorizationEndpoint.consentPage(CUSTOM_CONSENT_PAGE_URI))
                        .oidc(Customizer.withDefaults())
                )
                .exceptionHandling((conf) -> conf
                        .defaultAuthenticationEntryPointFor(
                                new LoginUrlAuthenticationEntryPoint("/user/login"),
                                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                        )
                );;
        return http.build();
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource() throws Exception {
        JWKSet jwkSet = new JWKSet(jwks.getRSAKey());
        return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
    }

    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> idTokenCustomizer() {
        return new FederatedIdentityIdTokenCustomizer();
    }

    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder().build();
    }
}
