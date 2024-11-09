package org.qbynet.authorization.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RegisteredClientObjectRepository extends MongoRepository<RegisteredClient, String> {
    Optional<RegisteredClient> findByClientId(String clientId);
}
