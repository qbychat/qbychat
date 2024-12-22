package org.qbynet.authorization.service.impl;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.qbynet.authorization.entity.JsonClient;
import org.qbynet.authorization.repository.JsonClientRepository;
import org.qbynet.authorization.service.RegisteredClientService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Log4j2
@Service
public class RegisteredClientServiceImpl implements RegisteredClientService {
    @Resource
    JsonClientRepository jsonClientRepository;

    @Resource
    PasswordEncoder passwordEncoder;

    @PostConstruct
    private void init() {
        if (jsonClientRepository.count() != 0) {
            return;
        }
        log.info("Add default clients to MongoDB...");
        RegisteredClient hoppscotch = RegisteredClient.withId(UUID.randomUUID().toString())
            .clientId("hoppscotch")
            .clientSecret(passwordEncoder.encode("secret"))
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
            .authorizationGrantType(AuthorizationGrantType.JWT_BEARER)
            .authorizationGrantType(AuthorizationGrantType.TOKEN_EXCHANGE)
            .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
            .redirectUri("https://hoppscotch.io/oauth")
            .scope(OidcScopes.OPENID)
            .scope(OidcScopes.PROFILE)
            .scope("bot.create")
            .scope("bot.delete")
            .scope("bot.list")
            .scope("profile.read")
            .scope("profile.edit")
            .scope("message.send")
            .scope("media.upload")
            .scope("conversation.join")
            .scope("conversation.list")
            .scope("conversation.create")
            .scope("avatar.manage")
            .scope("sticker.manage")
            .scope("search")
            .scope("link")
            .tokenSettings(TokenSettings.builder()
                .refreshTokenTimeToLive(Duration.ofDays(7))
                .accessTokenTimeToLive(Duration.ofDays(1))
                .build())
            .clientSettings(ClientSettings.builder().requireAuthorizationConsent(true).build())
            .build();

        RegisteredClient intellij = RegisteredClient.withId(UUID.randomUUID().toString())
            .clientId("intellij")
            .clientSecret(passwordEncoder.encode("secret"))
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
            .redirectUri("http://127.0.0.1:9000/authorized")
            .scope(OidcScopes.OPENID)
            .scope(OidcScopes.PROFILE)
            .scope("bot.create")
            .scope("bot.delete")
            .scope("bot.list")
            .scope("profile.read")
            .scope("profile.edit")
            .scope("message.send")
            .scope("media.upload")
            .scope("conversation.join")
            .scope("conversation.list")
            .scope("conversation.create")
            .scope("avatar.manage")
            .scope("sticker.manage")
            .scope("search")
            .scope("link")
            .tokenSettings(TokenSettings.builder()
                .refreshTokenTimeToLive(Duration.ofDays(7))
                .accessTokenTimeToLive(Duration.ofDays(1))
                .build())
            .clientSettings(ClientSettings.builder().requireAuthorizationConsent(false).build())
            .build();

        RegisteredClient qbychatAndroid = RegisteredClient.withId(UUID.randomUUID().toString())
            .clientId("qbychat-android")
            .clientSecret(passwordEncoder.encode("secret"))
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
            .authorizationGrantType(AuthorizationGrantType.JWT_BEARER)
            .authorizationGrantType(AuthorizationGrantType.TOKEN_EXCHANGE)
            .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
            .redirectUri("qbychat://oauth/authorize")
            .scope(OidcScopes.OPENID)
            .scope(OidcScopes.PROFILE)
            .scope("bot.create")
            .scope("bot.delete")
            .scope("bot.list")
            .scope("profile.read")
            .scope("profile.edit")
            .scope("message.send")
            .scope("media.upload")
            .scope("conversation.join")
            .scope("conversation.list")
            .scope("conversation.create")
            .scope("avatar.manage")
            .scope("sticker.manage")
            .scope("search")
            .scope("link")
            .tokenSettings(TokenSettings.builder()
                .refreshTokenTimeToLive(Duration.ofDays(7))
                .accessTokenTimeToLive(Duration.ofDays(1))
                .build())
            .clientSettings(ClientSettings.builder()
                .requireAuthorizationConsent(false)
                .build()
            )
            .build();
        save(hoppscotch);
        save(intellij);
        save(qbychatAndroid);
    }

    @Override
    public void save(RegisteredClient registeredClient) {
        jsonClientRepository.save(JsonClient.from(registeredClient));
    }

    @Override
    public JsonClient save(JsonClient jsonClient) {
        return jsonClientRepository.save(jsonClient);
    }

    @Override
    public RegisteredClient findById(String id) {
        return jsonClientRepository.findById(id).map(JsonClient::asRegisteredClient).orElse(null);
    }

    @Override
    public RegisteredClient findByClientId(String clientId) {
        return jsonClientRepository.findByClientId(clientId).map(JsonClient::asRegisteredClient).orElse(null);
    }
}
