package org.qbynet.authorization.service.impl;

import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import org.qbynet.authorization.entity.JsonAuthorization;
import org.qbynet.authorization.repository.JsonClientRepository;
import org.qbynet.authorization.repository.OAuth2AuthorizationRepository;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.stereotype.Service;

@Service
public class OAuth2AuthorizationServiceImpl implements OAuth2AuthorizationService {
    @Resource
    OAuth2AuthorizationRepository authorizationRepository;

    @Resource
    JsonClientRepository jsonClientRepository;

    @Override
    @SneakyThrows
    public void save(OAuth2Authorization authorization) {
        authorizationRepository.save(JsonAuthorization.from(authorization));
    }

    @Override
    public void remove(OAuth2Authorization authorization) {
        authorizationRepository.deleteById(authorization.getId());
    }

    @Override
    public OAuth2Authorization findById(String id) {
        return authorizationRepository.findById(id).map(it -> it.convent(jsonClientRepository.findByClientId(it.getRegisteredClientId()).orElseThrow().asRegisteredClient())).orElse(null);
    }

    @Override
    public OAuth2Authorization findByToken(String token, OAuth2TokenType tokenType) {
        return authorizationRepository.findAll().stream()
            .map(it -> it.convent(jsonClientRepository.findById(it.getRegisteredClientId())
                .orElseThrow().asRegisteredClient()))
            .filter(authorization -> {
                if (tokenType.getValue().equals("state")) {
                    return token.equals(authorization.getAttribute("state"));
                }
                return authorization.getToken(token) != null;
            })
            .findFirst()
            .orElse(null);
    }
}
