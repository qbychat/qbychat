package org.qbynet.chat.repository;

import org.qbynet.chat.entity.Conversation;
import org.qbynet.chat.entity.Member;
import org.qbynet.chat.entity.User;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface MemberRepository extends ReactiveMongoRepository<Member, String> {
    Mono<Member> findByConversationAndUser(Conversation conversation, User user);
}
