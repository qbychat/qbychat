package org.qbynet.chat.repository;

import org.qbynet.chat.entity.Message;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface MessageRepository extends MongoRepository<Message, String> {
    List<Message> findByExpiresAtNullOrExpiresAtGreaterThan(Instant now);
}
