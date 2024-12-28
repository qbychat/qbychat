package org.qbynet.chat.repository;

import org.qbynet.chat.entity.Member;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends ReactiveMongoRepository<Member, String> {
}
