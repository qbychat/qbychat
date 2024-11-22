package org.qbynet.authorization.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.stereotype.Repository;

@Repository
public interface OAuth2AuthorizationRepository extends MongoRepository<OAuth2Authorization, String> {
}
