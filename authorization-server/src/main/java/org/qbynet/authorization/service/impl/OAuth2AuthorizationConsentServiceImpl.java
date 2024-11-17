package org.qbynet.authorization.service.impl;

import jakarta.annotation.Resource;
import org.qbynet.authorization.repository.OAuth2AuthorizationConsentRepository;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsent;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.stereotype.Service;

@Service
public class OAuth2AuthorizationConsentServiceImpl implements OAuth2AuthorizationConsentService {
    @Resource
    OAuth2AuthorizationConsentRepository oAuth2AuthorizationConsentRepository;

    @Override
    public void save(OAuth2AuthorizationConsent authorizationConsent) {
        oAuth2AuthorizationConsentRepository.save(authorizationConsent);
    }

    @Override
    public void remove(OAuth2AuthorizationConsent authorizationConsent) {
        oAuth2AuthorizationConsentRepository.delete(authorizationConsent);
    }

    @Override
    public OAuth2AuthorizationConsent findById(String registeredClientId, String principalName) {
        return oAuth2AuthorizationConsentRepository.findFirstByRegisteredClientIdAndPrincipalName(registeredClientId, principalName).orElse(null);
    }
}
