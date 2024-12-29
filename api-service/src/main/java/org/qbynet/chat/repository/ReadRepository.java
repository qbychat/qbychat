package org.qbynet.chat.repository;

import org.qbynet.chat.entity.Member;
import org.qbynet.chat.entity.Message;
import org.qbynet.chat.entity.Read;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReadRepository extends MongoRepository<Read, String> {
    Optional<Read> findTopByMemberOrderByTimestampDesc(Member member);

    int countByMessage(Message message);
}
