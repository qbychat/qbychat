package org.qbynet.authorization.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsent;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OAuth2AuthorizationConsentRepository extends MongoRepository<OAuth2AuthorizationConsent, String> {
    Optional<OAuth2AuthorizationConsent> findFirstByRegisteredClientIdAndPrincipalName(String clientId, String principalName);
}
