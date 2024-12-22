package org.qbynet.chat.repository;

import org.qbynet.chat.entity.Avatar;
import org.qbynet.chat.entity.Conversation;
import org.qbynet.chat.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AvatarRepository extends MongoRepository<Avatar, String> {
    Optional<Avatar> findFirstByUser(User user);

    List<Avatar> findAllByUser(User user);

    Optional<Avatar> findFirstByConversation(Conversation conversation);

    List<Avatar> findAllByConversation(Conversation conversation);
}
