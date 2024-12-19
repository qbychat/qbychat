package org.qbynet.chat.repository;

import org.qbynet.chat.entity.Status;
import org.qbynet.chat.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StatusRepository extends MongoRepository<Status, String> {
    Optional<Status> findByUser(User user);
}
