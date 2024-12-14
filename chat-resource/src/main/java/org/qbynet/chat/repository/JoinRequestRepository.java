package org.qbynet.chat.repository;

import org.qbynet.chat.entity.Conversation;
import org.qbynet.chat.entity.JoinRequest;
import org.qbynet.chat.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JoinRequestRepository extends MongoRepository<JoinRequest, String> {
    List<JoinRequest> findAllByConversation(Conversation conversation);

    Optional<JoinRequest> findByConversationAndUser(Conversation conversation, User user);

    int countByConversation(Conversation conversation);
}
