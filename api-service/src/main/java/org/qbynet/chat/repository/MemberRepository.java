package org.qbynet.chat.repository;

import org.qbynet.chat.entity.Conversation;
import org.qbynet.chat.entity.Member;
import org.qbynet.chat.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends MongoRepository<Member, String> {
    Optional<Member> findByUserAndConversation(User user, Conversation conversation);

    List<Member> findAllByUser(User user);

    List<Member> findAllByConversation(Conversation conversation);

    List<Member> findAllByConversationAndBanUntilNullOrBanUntilGreaterThan(Conversation conversation, Instant banUntilAfter);

    List<Member> findAllByConversationAndPermissionsNotNullOrConversationAndOwnerIsTrue(Conversation conversation, Conversation same);

    void deleteAllByConversation(Conversation conversation);
}
