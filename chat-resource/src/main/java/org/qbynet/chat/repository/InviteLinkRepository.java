package org.qbynet.chat.repository;

import org.qbynet.chat.entity.InviteLink;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface InviteLinkRepository extends MongoRepository<InviteLink, Long> {
    Optional<InviteLink> findByLinkAndExpireAtAfter(String link, Instant expireAtAfter);
}
