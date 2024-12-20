package org.qbynet.authorization.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;

@Data
@Document(value = "oauthClient")
public class JsonClient implements Serializable {
    @Id
    private String id;
    @DBRef
    private Account owner;

    private String clientId;
    private Instant clientIdIssuedAt;
    private String clientSecret;

    private Instant clientSecretExpiresAt;

    private String clientName;

    private Set<ClientAuthenticationMethod> clientAuthenticationMethods;

    private Set<AuthorizationGrantType> authorizationGrantTypes;

    private Set<String> redirectUris;

    private Set<String> postLogoutRedirectUris;

    private Set<String> scopes;

    private ClientSettings clientSettings;

    private InternalTokenSettings tokenSettings;

    public static JsonClient from(RegisteredClient registeredClient, Account optionalOwner) {
        JsonClient jsonClient = new JsonClient();
        jsonClient.setId(registeredClient.getId());
        jsonClient.setClientId(registeredClient.getClientId());
        jsonClient.setClientSecret(registeredClient.getClientSecret());
        jsonClient.setClientSecretExpiresAt(registeredClient.getClientSecretExpiresAt());
        jsonClient.setClientName(registeredClient.getClientName());
        jsonClient.setClientAuthenticationMethods(registeredClient.getClientAuthenticationMethods());
        jsonClient.setAuthorizationGrantTypes(registeredClient.getAuthorizationGrantTypes());
        jsonClient.setRedirectUris(registeredClient.getRedirectUris());
        jsonClient.setPostLogoutRedirectUris(registeredClient.getPostLogoutRedirectUris());
        jsonClient.setScopes(registeredClient.getScopes());
        jsonClient.setClientSettings(registeredClient.getClientSettings());

        TokenSettings originTokenSettings = registeredClient.getTokenSettings();
        jsonClient.setTokenSettings(InternalTokenSettings.builder()
            .accessTokenTimeToLive(originTokenSettings.getAccessTokenTimeToLive())
            .accessTokenFormat(originTokenSettings.getAccessTokenFormat())
            .deviceCodeTimeToLive(originTokenSettings.getDeviceCodeTimeToLive())
            .reuseRefreshTokens(originTokenSettings.isReuseRefreshTokens())
            .x509CertificateBoundAccessTokens(originTokenSettings.isX509CertificateBoundAccessTokens())
            .idTokenSignatureAlgorithm(originTokenSettings.getIdTokenSignatureAlgorithm())
            .refreshTokenTimeToLive(originTokenSettings.getRefreshTokenTimeToLive())
            .authorizationCodeTimeToLive(originTokenSettings.getAuthorizationCodeTimeToLive())
            .build());

        jsonClient.setOwner(optionalOwner);
        return jsonClient;
    }

    public static JsonClient from(RegisteredClient registeredClient) {
        return from(registeredClient, null);
    }

    public RegisteredClient asRegisteredClient() {
        return RegisteredClient.withId(id)
            .clientId(clientId)
            .clientIdIssuedAt(clientIdIssuedAt)
            .clientSecret(clientSecret)
            .clientSecretExpiresAt(clientSecretExpiresAt)
            .clientName(clientName)
            .clientSettings(clientSettings)
            .tokenSettings(TokenSettings.builder()
                .accessTokenTimeToLive(this.tokenSettings.getAccessTokenTimeToLive())
                .accessTokenFormat(this.tokenSettings.getAccessTokenFormat())
                .deviceCodeTimeToLive(this.tokenSettings.getDeviceCodeTimeToLive())
                .reuseRefreshTokens(this.tokenSettings.isReuseRefreshTokens())
                .x509CertificateBoundAccessTokens(this.tokenSettings.isX509CertificateBoundAccessTokens())
                .idTokenSignatureAlgorithm(this.tokenSettings.getIdTokenSignatureAlgorithm())
                .refreshTokenTimeToLive(this.tokenSettings.getRefreshTokenTimeToLive())
                .authorizationCodeTimeToLive(this.tokenSettings.getAuthorizationCodeTimeToLive())
                .build())
            .scopes((it) -> it.addAll(scopes))
            .redirectUris((it) -> it.addAll(redirectUris))
            .postLogoutRedirectUris((it) -> it.addAll(postLogoutRedirectUris))
            .authorizationGrantTypes((it) -> it.addAll(authorizationGrantTypes))
            .clientAuthenticationMethods((it) -> it.addAll(clientAuthenticationMethods))
            .build();
    }

    @Data
    @Builder
    public static class InternalTokenSettings {
        private Duration authorizationCodeTimeToLive;
        private OAuth2TokenFormat accessTokenFormat;
        private Duration accessTokenTimeToLive;
        private Duration deviceCodeTimeToLive;
        private boolean reuseRefreshTokens;
        private Duration refreshTokenTimeToLive;
        private SignatureAlgorithm idTokenSignatureAlgorithm;
        private boolean x509CertificateBoundAccessTokens;
    }
}
