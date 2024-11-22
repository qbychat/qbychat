package org.qbynet.authorization.service.impl;

import jakarta.annotation.Resource;
import org.qbynet.authorization.repository.OAuth2AuthorizationRepository;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.stereotype.Service;

@Service
public class OAuth2AuthorizationServiceImpl implements OAuth2AuthorizationService {
    @Resource
    OAuth2AuthorizationRepository authorizationRepository;

    @Override
    public void save(OAuth2Authorization authorization) {
        authorizationRepository.save(authorization);
    }

    @Override
    public void remove(OAuth2Authorization authorization) {
        authorizationRepository.delete(authorization);
    }

    @Override
    public OAuth2Authorization findById(String id) {
        return authorizationRepository.findById(id).orElse(null);
    }

    @Override
    public OAuth2Authorization findByToken(String token, OAuth2TokenType tokenType) {
        return null; // todo
    }
}
