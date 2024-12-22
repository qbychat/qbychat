package org.qbynet.chat.repository;

import org.qbynet.chat.entity.TemporaryRelation;
import org.springframework.data.keyvalue.repository.KeyValueRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TemporaryRelationRepository extends KeyValueRepository<TemporaryRelation, String> {
    List<TemporaryRelation> findAllByOwner(String owner);

    Optional<TemporaryRelation> findByOwnerAndTarget(String owner, String target);
}
