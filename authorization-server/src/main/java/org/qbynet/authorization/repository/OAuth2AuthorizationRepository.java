package org.qbynet.authorization.repository;

import org.qbynet.authorization.entity.JsonAuthorization;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OAuth2AuthorizationRepository extends MongoRepository<JsonAuthorization, String> {
}
