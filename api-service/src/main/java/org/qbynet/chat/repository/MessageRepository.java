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
    Page<Message> findAllByIdGreaterThanAndConversationOrderBySentAtDesc(String id, Conversation conversation, Pageable pageable);

    List<Message> findAllByExpiresAtNullOrExpiresAtGreaterThan(Instant now);

    void deleteAllByExpiresAtLessThan(Instant now);

    Page<Message> findAllByConversationAndContentContainingIgnoreCaseAndExpiresAtNullOrExpiresAtGreaterThan(Conversation conversation, String content, Pageable pageable, Instant now);

    Page<Message> findAllByContentContainingIgnoreCaseAndExpiresAtNullOrExpiresAtGreaterThan(String content, Pageable pageable, Instant now);

    Page<Message> findAllByConversationOrderBySentAtDesc(Conversation conversation, Pageable pageable);
}
