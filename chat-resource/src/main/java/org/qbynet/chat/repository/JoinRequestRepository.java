package org.qbynet.chat.repository;

import org.qbynet.chat.entity.JoinRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JoinRequestRepository extends MongoRepository<JoinRequest, String> {
}
