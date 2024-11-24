package org.qbynet.chat.repository;

import org.qbynet.chat.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    boolean existsByRemoteId(String remoteId);

    Optional<User> findByRemoteId(String remoteId);
}
