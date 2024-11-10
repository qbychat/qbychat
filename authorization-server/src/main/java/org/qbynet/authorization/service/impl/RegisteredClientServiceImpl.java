package org.qbynet.authorization.service.impl;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.qbynet.authorization.repository.RegisteredClientObjectRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Log4j2
@Service
public class RegisteredClientServiceImpl implements RegisteredClientRepository {
    @Resource
    RegisteredClientObjectRepository registeredClientObjectRepository;

    @Value("${qbychat.client.web.address}")
    String webClientAddress;

    @PostConstruct
    private void init() {
        if (registeredClientObjectRepository.count() != 0) {
            return;
        }
        log.info("Add default clients to MongoDB...");
        RegisteredClient webClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("web-chat-oidc")
                .clientSecret("{noop}secret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .redirectUri(webClientAddress + "/login/oauth2/code/web-chat-oidc")
                .redirectUri(webClientAddress + "/authorized")
                .postLogoutRedirectUri(webClientAddress + "/logged-out")
                .scope(OidcScopes.OPENID)
                .scope(OidcScopes.PROFILE)
                .scope("message.read")
                .scope("message.write")
                .scope("user.read")
                .clientSettings(ClientSettings.builder().requireAuthorizationConsent(true).build())
                .build();
        save(webClient);
    }

    @Override
    public void save(RegisteredClient registeredClient) {
        registeredClientObjectRepository.save(registeredClient);
    }

    @Override
    public RegisteredClient findById(String id) {
        return registeredClientObjectRepository.findById(id).orElse(null);
    }

    @Override
    public RegisteredClient findByClientId(String clientId) {
        return registeredClientObjectRepository.findByClientId(clientId).orElse(null);
    }
}
