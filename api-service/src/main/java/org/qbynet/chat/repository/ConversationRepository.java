package org.qbynet.chat.repository;

import org.qbynet.chat.entity.Conversation;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConversationRepository extends ReactiveMongoRepository<Conversation, String> {
}
