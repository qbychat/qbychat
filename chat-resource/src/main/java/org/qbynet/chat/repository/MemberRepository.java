package org.qbynet.chat.repository;

import org.qbynet.chat.entity.Conversation;
import org.qbynet.chat.entity.Member;
import org.qbynet.chat.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends MongoRepository<Member, String> {
    Optional<Member> findByUserAndConversation(User user, Conversation conversation);
}
