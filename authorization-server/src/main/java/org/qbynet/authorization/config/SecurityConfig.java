package org.qbynet.authorization.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import jakarta.annotation.Resource;
import org.qbynet.authorization.authentication.DeviceClientAuthenticationConverter;
import org.qbynet.authorization.authentication.DeviceClientAuthenticationProvider;
import org.qbynet.authorization.jose.Jwks;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

import java.util.Set;
import java.util.stream.Collectors;

@EnableWebSecurity
@Configuration(proxyBeanMethods = false)
public class SecurityConfig {
    private static final String CUSTOM_CONSENT_PAGE_URI = "/oauth2/consent";

    @Resource
    Jwks jwks;

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Order(1)
    SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http, RegisteredClientRepository registeredClientRepository, AuthorizationServerSettings authorizationServerSettings) throws Exception {
        DeviceClientAuthenticationConverter deviceClientAuthenticationConverter =
            new DeviceClientAuthenticationConverter(
                authorizationServerSettings.getDeviceAuthorizationEndpoint());
        DeviceClientAuthenticationProvider deviceClientAuthenticationProvider =
            new DeviceClientAuthenticationProvider(registeredClientRepository);

        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
            OAuth2AuthorizationServerConfigurer.authorizationServer();
        http
            .securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
            .with(authorizationServerConfigurer, Customizer.withDefaults())
            .authorizeHttpRequests((authorize) ->
                authorize.anyRequest().authenticated()
            );
        http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
            .deviceAuthorizationEndpoint(deviceAuthorizationEndpoint ->
                deviceAuthorizationEndpoint.verificationUri("/activate")
            )
            .deviceVerificationEndpoint(deviceVerificationEndpoint ->
                deviceVerificationEndpoint.consentPage(CUSTOM_CONSENT_PAGE_URI)
            )
            .clientAuthentication(clientAuthentication ->
                clientAuthentication
                    .authenticationConverter(deviceClientAuthenticationConverter)
                    .authenticationProvider(deviceClientAuthenticationProvider)
            )
            .authorizationEndpoint(authorizationEndpoint ->
                authorizationEndpoint.consentPage(CUSTOM_CONSENT_PAGE_URI))
            .oidc(Customizer.withDefaults());
        http
            .oauth2ResourceServer(conf -> conf
                .jwt(Customizer.withDefaults())
            )
            .exceptionHandling((conf) -> conf
                .defaultAuthenticationEntryPointFor(
                    new LoginUrlAuthenticationEntryPoint("/user/login"),
                    new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                )
            );
        return http.build();
    }

    @Bean
    @Order(2)
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
            .authorizeHttpRequests(conf -> conf
                .requestMatchers("/actuator", "/actuator/**").permitAll()
                .requestMatchers("/assets/**", "/favicon.ico", "/user/login").permitAll()
                .requestMatchers("/user/register", "/user/register/admin", "/user/confirm").anonymous()
                .anyRequest().authenticated()
            )
            .formLogin(conf -> conf
                .loginPage("/user/login")
            )
            .logout(conf -> conf
                .logoutUrl("/user/logout")
                .logoutSuccessUrl("/user/login?logout")
            )
            .build();
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource() throws Exception {
        JWKSet jwkSet = new JWKSet(jwks.getRSAKey());
        return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
    }

    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder().build();
    }

    @Bean
    OAuth2TokenCustomizer<JwtEncodingContext> jwtCustomizer() {
        return context -> {
            if (context.getTokenType() == OAuth2TokenType.ACCESS_TOKEN) {
                Authentication principal = context.getPrincipal();
                Set<String> authorities = principal.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toSet());
                context.getClaims().claim("roles", authorities);
            }
        };
    }
}
