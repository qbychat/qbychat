package org.qbynet.authorization.repository;

import org.qbynet.authorization.entity.Account;
import org.qbynet.authorization.entity.JsonClient;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JsonClientRepository extends MongoRepository<JsonClient, String> {
    Optional<JsonClient> findByClientId(String clientId);

    List<JsonClient> findAllByOwner(Account owner);

    boolean existsByClientId(String clientId);
}
