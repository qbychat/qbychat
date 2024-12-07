package org.qbynet.chat.repository;

import org.qbynet.chat.entity.Conversation;
import org.qbynet.chat.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface MessageRepository extends MongoRepository<Message, String> {
    List<Message> findByExpiresAtNullOrExpiresAtGreaterThan(Instant now);

    void deleteByExpiresAtLessThan(Instant now);

    Page<Message> findAllByConversationAndContentContainingIgnoreCase(Conversation conversation, String content, Pageable pageable);

    Page<Message> findAllByContentContainingIgnoreCase(String content, Pageable pageable);
}
