package org.qbynet.chat.repository;

import org.qbynet.chat.entity.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConversationRepository extends MongoRepository<Conversation, String> {
    Optional<Conversation> findByLink(String link);

    Page<Conversation> findAllByLinkStartsWithIgnoreCase(String link, Pageable pageable);

    Page<Conversation> findAllByNameContainingIgnoreCase(String name, Pageable pageable);
}
