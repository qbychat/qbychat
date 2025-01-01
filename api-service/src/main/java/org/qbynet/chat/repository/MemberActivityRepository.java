package org.qbynet.chat.repository;

import org.qbynet.chat.entity.MemberActivity;
import org.springframework.data.keyvalue.repository.KeyValueRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberActivityRepository extends KeyValueRepository<MemberActivity, String> {
    Optional<MemberActivity> findByUserAndConversation(String user, String conversation);

    List<MemberActivity> findAllByConversation(String conversation);
}
