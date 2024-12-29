package org.qbynet.authorization.service.impl;

import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.qbynet.authorization.entity.Account;
import org.qbynet.authorization.entity.JsonClient;
import org.qbynet.authorization.entity.dto.RegisterAppDTO;
import org.qbynet.authorization.repository.JsonClientRepository;
import org.qbynet.authorization.service.RegisteredClientService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Log4j2
@Service
public class RegisteredClientServiceImpl implements RegisteredClientService {
    @Resource
    JsonClientRepository jsonClientRepository;

    @Resource
    PasswordEncoder passwordEncoder;

    @Override
    public void save(RegisteredClient registeredClient) {
        jsonClientRepository.save(JsonClient.from(registeredClient));
    }

    @Override
    public RegisteredClient findById(String id) {
        return jsonClientRepository.findById(id).map(JsonClient::asRegisteredClient).orElse(null);
    }

    @Override
    public RegisteredClient findByClientId(String clientId) {
        return jsonClientRepository.findByClientId(clientId).map(JsonClient::asRegisteredClient).orElse(null);
    }

    @Override
    public List<JsonClient> findAllByOwner(Account account) {
        return jsonClientRepository.findAllByOwner(account);
    }

    @Override
    public @NotNull JsonClient register(@NotNull RegisterAppDTO data, Account account) {
        // check client id available
        if (jsonClientRepository.existsByClientId(data.getClientId())) {
            throw new IllegalArgumentException("Client id already exists");
        }
        RegisteredClient client = RegisteredClient.withId(UUID.randomUUID().toString())
            .clientId(data.getClientId())
            .clientName(data.getClientName())
            .clientSecret(passwordEncoder.encode(data.getClientSecret()))
            .clientAuthenticationMethods(methods -> methods.addAll(data.getClientAuthenticationMethods().stream().map(ClientAuthenticationMethod::new).toList()))
            .authorizationGrantTypes(types -> types.addAll(data.getAuthorizationGrantTypes().stream().map(AuthorizationGrantType::new).toList()))
            .postLogoutRedirectUris(uris -> uris.addAll(data.getPostLogoutRedirectUris()))
            .redirectUris(uris -> uris.addAll(data.getRedirectUris()))
            .scopes(scopes -> scopes.addAll(data.getScopes()))
            .tokenSettings(TokenSettings.builder()
                .refreshTokenTimeToLive(Duration.ofDays(7))
                .accessTokenTimeToLive(Duration.ofDays(1))
                .build())
            .clientSettings(ClientSettings.builder()
                .requireAuthorizationConsent(false)
                .build())
            .build();
        JsonClient jsonClient = JsonClient.from(client);
        jsonClient.setOwner(account);
        log.info("App {} was registered", client.getClientName());
        return jsonClientRepository.save(jsonClient);
    }
}
